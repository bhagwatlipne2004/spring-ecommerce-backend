# Learnings

## 2026-08-06

### LazyInitializationException

Problem:
- Category was LAZY loaded.
- Mapper accessed category after the session closed.

Solution:
- Added @Transactional(readOnly = true).

Lesson:
- Understand why LAZY loading behaves this way instead of switching to EAGER.

---

### Update Logic

Problem:
- Created a new Product object during update.

Solution:
- Update the managed entity instead.

Lesson:
- Hibernate tracks managed entities. Updating fields is preferred over creating a new object.

---

### REST API Design

Instead of:

GET /products/search
GET /products/category

Prefer:

GET /products?keyword=iphone&categoryId=1