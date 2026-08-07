# Common Mistakes

## Returning Entity directly

Use DTO instead.

---

## Using Optional.get()

Use orElseThrow().

---

## Business Logic in Controller

Move to Service.

---

## Using FetchType.EAGER everywhere

Prefer LAZY.

---

## Forgetting @Transactional

Can cause LazyInitializationException.

---

## Duplicate Validation

Always check duplicate names before create/update.

---

## Returning List instead of Page

Use Page when pagination is required.

---

## Exposing Internal Exception Messages

Use Global Exception Handler.