const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp(offsetMs = 2000) {
  return new Date(Date.now() - offsetMs);
}

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

test("Unauthenticated user cannot read or write any user profile, trips, or bookings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("trips").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bookings").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("saved_items").get());
});

test("Authenticated owner can create, update, and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const createdAt = pastTimestamp(5000);
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Alice Traveler",
      preferredCurrency: "USD",
      temperatureUnit: "METRIC",
      preferredCabinClass: "BUSINESS",
      homeAirportIata: "SFO",
      biometricThresholdUsd: 500,
      darkGlassTheme: true,
      createdAt,
      updatedAt: createdAt,
    })
  );
  await assertSucceeds(profileRef.get());
  await assertSucceeds(
    profileRef.update({
      displayName: "Alice Updated",
      updatedAt: pastTimestamp(1000),
    })
  );
});

test("Authenticated owner can create, read, update, and delete trips and saved items", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const createdAt = pastTimestamp(5000);

  const tripRef = aliceDb.collection("users").doc(ALICE_UID).collection("trips").doc("trip_1");
  await assertSucceeds(
    tripRef.set({
      userId: ALICE_UID,
      title: "Kyoto Serenity",
      destination: "Kyoto, Japan",
      startDate: "2026-12-10",
      endDate: "2026-12-16",
      travellersCount: 2,
      budgetUsd: 4200,
      style: "Luxury & Wellness",
      status: "PLANNED",
      createdAt,
      updatedAt: createdAt,
    })
  );
  await assertSucceeds(tripRef.get());
  await assertSucceeds(tripRef.update({ status: "UPCOMING", updatedAt: pastTimestamp(1000) }));

  const savedRef = aliceDb.collection("users").doc(ALICE_UID).collection("saved_items").doc("sav_1");
  await assertSucceeds(
    savedRef.set({
      userId: ALICE_UID,
      itemType: "HOTEL",
      title: "Aman Tokyo Sanctuary",
      subtitle: "Otemachi, Tokyo",
      category: "Luxury",
      priceLabel: "$620/night",
      rating: 4.9,
      createdAt,
      updatedAt: createdAt,
    })
  );
  await assertSucceeds(savedRef.delete());
});

test("Cross-user access to trips and bookings is rejected", async () => {
  const createdAt = pastTimestamp(5000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(BOB_UID).collection("bookings").doc("bk_1").set({
      userId: BOB_UID,
      referenceCode: "ALR-999-FLT",
      bookingType: "FLIGHT",
      title: "SFO to NRT",
      providerSource: "Amadeus GDS",
      originOrLocation: "SFO",
      totalAmountUsd: 920,
      status: "CONFIRMED",
      sha256AuditHash: "1234567890abcdef",
      createdAt,
      updatedAt: createdAt,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("bookings").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("bookings").doc("bk_1").get());
});

test("Shadow update with ghost field on UserProfile is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp(5000);
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      preferredCurrency: "USD",
      temperatureUnit: "METRIC",
      preferredCabinClass: "ECONOMY",
      homeAirportIata: "SFO",
      biometricThresholdUsd: 250,
      darkGlassTheme: true,
      isAdmin: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Cancelled booking cannot be modified (terminal state lock) and bookings cannot be deleted", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp(5000);
  const bookingRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_cancel");
  await assertSucceeds(
    bookingRef.set({
      userId: ALICE_UID,
      referenceCode: "ALR-123-HOT",
      bookingType: "HOTEL",
      title: "Aman Tokyo",
      providerSource: "Expedia EPS Rapid",
      originOrLocation: "Tokyo",
      totalAmountUsd: 620,
      status: "CANCELLED",
      sha256AuditHash: "abcdef1234567890",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(bookingRef.update({ status: "CONFIRMED", updatedAt: pastTimestamp(1000) }));
  await assertFails(bookingRef.delete());
});
