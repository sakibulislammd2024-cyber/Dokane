const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_owner";
const BOB_UID = "bob_outsider";

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

test("Unauthenticated user cannot read or write stores or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("stores").doc("store_1").get());
  await assertFails(
    unauthDb.collection("stores").doc("store_1").collection("products").get()
  );
});

test("User can create and read own profile but cannot read or modify another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      email: "alice@example.com",
      storeId: "store_1",
      role: "owner",
      isSimpleMode: false,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set({ storeId: "hacked" })
  );
});

test("Owner can create store and products, outsider cannot read or write", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("stores").doc("store_1").set({
      name: "Alice Store",
      phone: "01700000000",
      address: "Dhaka",
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID],
      members: [{ userId: ALICE_UID, role: "owner", status: "active" }],
    })
  );

  await assertSucceeds(
    aliceDb
      .collection("stores")
      .doc("store_1")
      .collection("products")
      .doc("prod_1")
      .set({
        storeId: "store_1",
        name: "Rice 5kg",
        sellPrice: 450,
        stock: 20,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("stores")
      .doc("store_1")
      .collection("products")
      .get()
  );

  await assertFails(bobDb.collection("stores").doc("store_1").get());
  await assertFails(
    bobDb
      .collection("stores")
      .doc("store_1")
      .collection("products")
      .doc("prod_1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("stores")
      .doc("store_1")
      .collection("products")
      .doc("prod_2")
      .set({ storeId: "store_1", name: "Rogue" })
  );
});
