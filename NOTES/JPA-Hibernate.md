# Spring Data JPA

## JpaRepository

Provides CRUD operations.

Examples:

save()

findById()

findAll()

delete()

deleteById()

---

## Derived Query Methods

Examples:

findByName()

findByCategoryId()

findByNameContainingIgnoreCase()

Spring generates SQL automatically.

---

## Pagination

Repository

```java
findAll(Pageable pageable)
```

Create Pageable

```java
PageRequest.of(page, size, sort)
```

Return

```java
Page<Product>
```

Useful Methods

getContent()

getTotalPages()

getTotalElements()

isFirst()

isLast()

---

## Sorting

```java
Sort.by(sortBy).ascending()
```

```java
Sort.by(sortBy).descending()
```

---

## Search

```java
findByNameContainingIgnoreCase()
```

Equivalent SQL

```sql
WHERE LOWER(name)
LIKE LOWER('%keyword%')
```
# Hibernate Notes

## Entity

Represents a database table.

Example:

Product

Category

---

## @ManyToOne

Example

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name="category_id")
private Category category;
```

Many Products

↓

One Category

---

## @JoinColumn

Specifies the foreign key column.

Example

```java
@JoinColumn(name="category_id")
```

Database

products.category_id

↓

categories.id

---

## FetchType.LAZY

Related entity loads only when accessed.

Advantages

- Better Performance
- Less memory usage

---

## FetchType.EAGER

Loads immediately.

Easy

But slower for large applications.

---

## LazyInitializationException

Occurs when

Session closed

↓

Lazy object accessed.

Solutions

- @Transactional
- JOIN FETCH
- EntityGraph

---

## Dirty Checking

Hibernate tracks managed entities.

Example

```java
product.setPrice(1000);
```

No explicit update required.

Hibernate updates automatically when transaction commits.


# Specifications

## Why use Specifications?

Avoid creating many repository methods.

Instead of:

findByCategoryIdAndActiveTrueAndPriceBetween(...)

Create small reusable Specifications.

Example:

hasCategory()

hasKeyword()

isActive()

priceGreaterThan()

Combine them dynamically:

```java
Specification<Product> specification = Specification.unrestricted();

specification = specification.and(hasCategory(categoryId));

specification = specification.and(hasKeyword(keyword));

specification = specification.and(isActive(active));
```

Execute:

```java
repository.findAll(specification, pageable);
```

Benefits:

- Cleaner repository
- Reusable filters
- Supports dynamic search
- Easy to extend