const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_ce_2026";
const BOB_UID = "bob_ce_2026";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- 1. UNAUTHENTICATED ACCESS DENIAL ---
test("Unauthenticated user cannot read or write profiles, personal_books, or study_notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Unauthorized",
      curriculumYear: 1,
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("personal_books").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("study_notes").get()
  );
});

// --- 2. USER PROFILE CRUD, PII ISOLATION, & SHADOW UPDATE ---
test("Owner can create and update their UserProfile; cross-user and shadow updates fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Create valid profile matching Kotlin UserProfile create map (no 'id' field)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Maria Santos",
      email: "maria@example.com",
      studyMantra: "BS Civil Engineering · Year 2",
      curriculumYear: 2,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Owner read succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Cross-user PII read & write fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Valid partial update preserving createdAt and userId
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      studyMantra: "Structural & Geotechnical Track",
      curriculumYear: 3,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Shadow update with ghost field fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Tampered immutable createdAt fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Tampered future timestamp fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      curriculumYear: 4,
      updatedAt: new Date(Date.now() + 120000),
    })
  );
});

// --- 3. PERSONAL BOOKS SUBCOLLECTION (BOOKMARKS & READING PROGRESS) ---
test("Owner can bookmark, list, update progress, and delete personal_books; cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 8000);
  const bookId = "ia_civilengineering029725mbp";

  // Mirror Kotlin PersonalBook create payload (no @DocumentId `id` field, uses `bookmarked` / `completed`)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_books")
      .doc(bookId)
      .set({
        userId: ALICE_UID,
        bookId: bookId,
        title: "Civil Engineering Handbook (4th Edition)",
        author: "Leonard Church Urquhart",
        subjectKey: "STRUCTURAL",
        courseCode: "CE 301",
        coverUrl: "https://archive.org/services/img/civilengineering029725mbp",
        coverArtKey: "structural",
        totalPages: 1174,
        currentPage: 1,
        bookmarked: true,
        completed: false,
        lastOpenedAt: Date.now() - 1000,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Mirror Kotlin subcollection list query
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("personal_books").get()
  );

  // Mirror Kotlin reading progress partial update
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_books")
      .doc(bookId)
      .update({
        currentPage: 42,
        totalPages: 1174,
        lastOpenedAt: Date.now(),
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Mirror Kotlin bookmark / completion toggle update
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_books")
      .doc(bookId)
      .update({
        bookmarked: true,
        completed: true,
        updatedAt: new Date(Date.now() - 500),
      })
  );

  // Reject extra @DocumentId `id` field on create
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_books")
      .doc("book_with_extra_id")
      .set({
        id: "book_with_extra_id",
        userId: ALICE_UID,
        bookId: "book_with_extra_id",
        title: "Bad Payload",
        author: "Test",
        subjectKey: "STRUCTURAL",
        courseCode: "CE 301",
        coverArtKey: "structural",
        totalPages: 200,
        currentPage: 1,
        bookmarked: true,
        completed: false,
        lastOpenedAt: 0,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Cross-user read, list, update, delete fail
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("personal_books").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_books")
      .doc(bookId)
      .delete()
  );

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_books")
      .doc(bookId)
      .delete()
  );
});

// --- 4. STUDY NOTES SUBCOLLECTION ---
test("Owner can create, list, update, and delete study_notes; invalid bounds & cross-user fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 6000);
  const noteId = "note_ce301_p42";

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("study_notes")
      .doc(noteId)
      .set({
        userId: ALICE_UID,
        bookId: "ia_civilengineering029725mbp",
        bookTitle: "Civil Engineering Handbook (4th Edition)",
        pageNumber: 42,
        chapterTitle: "Page 42",
        noteText: "Moment distribution formula for continuous beams",
        createdAt: created,
        updatedAt: created,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_notes").get()
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("study_notes")
      .doc(noteId)
      .update({
        noteText: "Updated: Carry-over factor = 0.5 for prismatic beams",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Oversized noteText (> 2000 chars) must fail
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("study_notes")
      .doc(noteId)
      .update({
        noteText: "x".repeat(2500),
        updatedAt: new Date(Date.now() - 500),
      })
  );

  // Cross-user list and delete fail
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("study_notes").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("study_notes")
      .doc(noteId)
      .delete()
  );

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("study_notes")
      .doc(noteId)
      .delete()
  );
});
