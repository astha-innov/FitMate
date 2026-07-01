require("dotenv").config();

const fs = require("fs");
const path = require("path");
const cloudinary = require("cloudinary").v2;

const { initializeApp, cert, getApps } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

const ROOT = process.cwd();

const MANIFEST_PATH = path.join(
  ROOT,
  "app/src/main/assets/exercise_asset_manifest.json"
);

const IMAGE_DIR = path.join(ROOT, "app/src/main/assets/exercises");
const GIF_DIR = path.join(ROOT, "app/src/main/assets/workout_details/gifs");

const DRY_RUN = !process.argv.includes("--run");

cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET,
});

function checkRequiredEnv() {
  const requiredKeys = [
    "CLOUDINARY_CLOUD_NAME",
    "CLOUDINARY_API_KEY",
    "CLOUDINARY_API_SECRET",
    "FIREBASE_SERVICE_ACCOUNT",
  ];

  for (const key of requiredKeys) {
    if (!process.env[key]) {
      throw new Error(`Missing ${key} in .env file`);
    }
  }
}

function loadManifest() {
  if (!fs.existsSync(MANIFEST_PATH)) {
    throw new Error(`Manifest file not found at: ${MANIFEST_PATH}`);
  }

  const rawData = fs.readFileSync(MANIFEST_PATH, "utf-8");
  const manifest = JSON.parse(rawData);

  if (Array.isArray(manifest)) {
    return {
      originalManifest: manifest,
      exercises: manifest,
      isArrayManifest: true,
    };
  }

  if (Array.isArray(manifest.exercises)) {
    return {
      originalManifest: manifest,
      exercises: manifest.exercises,
      isArrayManifest: false,
    };
  }

  throw new Error(
    "Invalid manifest format. Expected an array or an object containing an exercises array."
  );
}

function saveManifest(originalManifest, exercises, isArrayManifest) {
  const updatedManifest = isArrayManifest
    ? exercises
    : {
        ...originalManifest,
        exercises,
      };

  fs.writeFileSync(MANIFEST_PATH, JSON.stringify(updatedManifest, null, 2));
}

function resolveLocalFile(baseDir, fileName) {
  if (!fileName) return null;

  const filePath = path.join(baseDir, fileName);

  if (fs.existsSync(filePath)) {
    return filePath;
  }

  return null;
}

async function uploadToCloudinary(localFilePath, publicId) {
  if (!localFilePath) return null;

  if (DRY_RUN) {
    console.log(`[DRY RUN] Would upload: ${localFilePath}`);
    console.log(`[DRY RUN] Cloudinary public_id: ${publicId}`);

    return {
      secure_url: "",
      public_id: publicId,
    };
  }

  return await cloudinary.uploader.upload(localFilePath, {
    public_id: publicId,
    resource_type: "image",
    overwrite: true,
    invalidate: true,
  });
}

function initializeFirebase() {
  if (DRY_RUN) return null;

  const serviceAccountPath = path.isAbsolute(process.env.FIREBASE_SERVICE_ACCOUNT)
    ? process.env.FIREBASE_SERVICE_ACCOUNT
    : path.join(ROOT, process.env.FIREBASE_SERVICE_ACCOUNT);

  if (!fs.existsSync(serviceAccountPath)) {
    throw new Error(
      `Firebase service account file not found at: ${serviceAccountPath}`
    );
  }

  const serviceAccount = require(serviceAccountPath);

  if (!getApps().length) {
    initializeApp({
      credential: cert(serviceAccount),
    });
  }

  return getFirestore();
}

async function main() {
  checkRequiredEnv();

  const { originalManifest, exercises, isArrayManifest } = loadManifest();
  const db = initializeFirebase();

  let processedCount = 0;
  let uploadedThumbnails = 0;
  let uploadedDetailMedia = 0;

  const missingThumbnails = [];
  const missingDetailMedia = [];
  const skippedEntries = [];

  for (const exercise of exercises) {
    const exerciseId = exercise.exerciseId;

    if (!exerciseId) {
      skippedEntries.push(exercise);
      console.warn("\nSkipping entry because exerciseId is missing:");
      console.warn(exercise);
      continue;
    }

    console.log(`\nProcessing exercise: ${exerciseId}`);

    const thumbnailPath = resolveLocalFile(IMAGE_DIR, exercise.thumbnailFile);
    const detailMediaPath = resolveLocalFile(GIF_DIR, exercise.detailMediaFile);

    if (!thumbnailPath) {
      missingThumbnails.push(exerciseId);
      console.warn(`Missing thumbnail/image for: ${exerciseId}`);
    }

    if (!detailMediaPath) {
      missingDetailMedia.push(exerciseId);
      console.warn(`Missing detail GIF/media for: ${exerciseId}`);
    }

    const thumbnailPublicId = `workouts/images/${exerciseId}`;
    const detailMediaPublicId = `workouts/gifs/${exerciseId}`;

    const thumbnailUpload = await uploadToCloudinary(
      thumbnailPath,
      thumbnailPublicId
    );

    const detailMediaUpload = await uploadToCloudinary(
      detailMediaPath,
      detailMediaPublicId
    );

    if (thumbnailUpload?.secure_url) {
      exercise.thumbnailUrl = thumbnailUpload.secure_url;
      uploadedThumbnails++;
    }

    if (detailMediaUpload?.secure_url) {
      exercise.detailMediaUrl = detailMediaUpload.secure_url;
      uploadedDetailMedia++;
    }

    const firestoreData = {
      name: exercise.name || exerciseId,
      thumbnailUrl: exercise.thumbnailUrl || "",
      detailMediaUrl: exercise.detailMediaUrl || "",
    };

    if (DRY_RUN) {
      console.log("[DRY RUN] Would write to Firestore:");
      console.log(`Collection: exercises`);
      console.log(`Document ID: ${exerciseId}`);
      console.log(firestoreData);
    } else {
      await db.collection("exercises").doc(exerciseId).set(firestoreData);
      console.log(`Firestore updated: exercises/${exerciseId}`);
    }

    processedCount++;
  }

  if (!DRY_RUN) {
    saveManifest(originalManifest, exercises, isArrayManifest);
    console.log("\nManifest updated with Cloudinary URLs.");
  }

  console.log("\nMigration finished.");
  console.log(`Mode: ${DRY_RUN ? "DRY RUN" : "LIVE RUN"}`);
  console.log(`Exercises processed: ${processedCount}`);
  console.log(`Thumbnails uploaded: ${uploadedThumbnails}`);
  console.log(`Detail media uploaded: ${uploadedDetailMedia}`);
  console.log(`Missing thumbnails: ${missingThumbnails.length}`);
  console.log(`Missing detail media: ${missingDetailMedia.length}`);
  console.log(`Skipped entries: ${skippedEntries.length}`);

  if (missingThumbnails.length > 0) {
    console.log("\nExercises missing thumbnails/images:");
    console.log(missingThumbnails);
  }

  if (missingDetailMedia.length > 0) {
    console.log("\nExercises missing detail GIF/media:");
    console.log(missingDetailMedia);
  }

  if (skippedEntries.length > 0) {
    console.log("\nSkipped entries:");
    console.log(skippedEntries);
  }
}

main().catch((error) => {
  console.error("\nMigration failed:");
  console.error(error);
  process.exit(1);
});