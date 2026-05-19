# Entity Relationship Diagram (ERD)

The core domain model in assignP includes the following entities:

- **User**: application users with roles `ADMIN` or `MEMBER`.
- **Project**: a project with an owner and many members.
- **Task**: a task assigned to a project and optionally to a user.
- **TaskComment**: comments attached to a task.
- **TaskActivity**: audit entries for task lifecycle events.
- **Notification**: notification entries for users.

## Relationships

- `User` 1..* `Project` as owner.
- `Project` *..* `User` as members.
- `Project` 1..* `Task`.
- `Task` 0..1 `User` as assignee.
- `Task` 0..* `TaskComment`.
- `Task` 0..* `TaskActivity`.
- `User` 0..* `Notification`.

## ERD Summary

- `User` --> `Project.owner`
- `Project.members` <---> `User`
- `Project` --> `Task.project`
- `Task` --> `User.assignee`
- `TaskComment.task` --> `Task`
- `TaskActivity.task` --> `Task`
- `Notification.user` --> `User`
