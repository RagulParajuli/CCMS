# Local record database

CCMS stores dashboard data in a local Room database (`ccms.db`) on the device. Firebase is
used for authentication only; there was no Firestore/Realtime Database in the project before
this feature, so no cloud database setup is required.

## Tables

| Table | Contents |
| --- | --- |
| `memberships` | Member name, phone, status, notes, join date |
| `loan_deposits` | Loan/deposit kind, member name, amount, notes, date |
| `monthly_dues` | Title, period, amount, paid/pending status, notes, date |
| `history` | Activity log written automatically on add, edit and delete |

Every row has an `ownerId` column set to the signed-in Firebase `uid`. Queries and deletes are
scoped to that id, so two accounts on the same device do not share records.

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

The database is version 1 with `exportSchema = false`. Add a proper Room migration (and enable
schema export) before changing the schema in a released build. Clearing app data or
uninstalling the app deletes the local records; cloud backup is not implemented.
