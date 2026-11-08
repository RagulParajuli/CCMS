# Records storage and synchronization

Firestore is the shared cloud record store across devices signed in to the same Firebase
account. Room (`ccms.db`) is its local replica for immediate offline reads and writes. Every
screen continues to observe the existing Room flows, so the UI and count calculations use one
consistent local model. Firestore's Android SDK persistence queues cloud writes while offline.

## Tables

| Table | Contents |
| --- | --- |
| `memberships` | Member name, phone, status, notes, join date |
| `loan_deposits` | Loan/deposit kind, member name, amount, notes, date |
| `monthly_dues` | Title, period, amount, paid/pending status, notes, date |
| `history` | Activity log written automatically on add, edit and delete |

Every row has an `ownerId` column set to the signed-in Firebase `uid`. Queries and deletes are
scoped to that id, so two accounts on the same device do not share records. Each row also has
a stable `syncId`; Room's numeric primary keys remain local to the device, while `syncId` is
the Firestore document ID. Existing version 1 databases are migrated to version 2 and receive
stable IDs before synchronization.

## Firestore layout and sync behavior

The app stores documents under `users/{uid}/memberships`, `users/{uid}/loanDeposits`,
`users/{uid}/monthlyDues`, and `users/{uid}/history`. Record changes are written to Room in a
transaction with their history entry, then submitted to Firestore. Firestore snapshot listeners
apply additions, edits, and removals back into Room. On the first server snapshot, records that
exist only in Room are uploaded; records that exist in both places use the latest `updatedAt`
(history uses `timestamp`).

Firestore is initialized only when a Firebase app is configured, so Room-only development
builds still work. The deployed Firestore Security Rules must permit an authenticated user to
read and write only their own `users/{uid}` subcollections. Rules are not managed by the app and
were not changed as part of this integration.

## Live counts

`RecordsDao` exposes `Flow<Int>` count queries for the three record tables.
`RecordsRepository.observeCounts()` combines them into `RecordCounts`, and
`RecordsViewModel` exposes that as a `StateFlow`. The dashboard cards, sidebar badges, About
App module list and pie chart all read the same state, so a Room invalidation (insert, update
or delete) refreshes every count automatically. No counts are hardcoded; an empty table
naturally produces `0`.

## Writes and history

All writes go through `RecordsRepository`, which uses a Room transaction to save the row and
append a history entry. Deleting a row also appends a history entry. `History → Clear` only
removes the activity log; it never deletes record rows.

## Scope and migration

The database is version 2 with `exportSchema = false`. Clearing app data or uninstalling the
app deletes the local replica; records can be restored from Firestore after signing in again.
