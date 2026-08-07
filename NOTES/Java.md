# Java Notes

## Optional

### Why Optional?

Used to avoid NullPointerException.

Example:

```java
Optional<Product> product = productRepository.findById(id);
```

### Useful Methods

```java
isPresent()
```

Checks whether a value exists.

```java
orElseThrow()
```

Throws an exception if the value is absent.

Example:

```java
Product product = productRepository.findById(id)
        .orElseThrow(() ->
                new ResourceNotFoundException("Product", "id", id));
```

Avoid:

```java
optional.get();
```

unless you're sure the value exists.

---

## Streams

Purpose:

Process collections.

Example:

```java
products.stream()
        .map(productMapper::toResponse)
        .toList();
```

Equivalent to:

```java
List<ProductResponse> responses = new ArrayList<>();

for(Product product : products){
    responses.add(productMapper.toResponse(product));
}
```

### map()

Transforms one object into another.

Product

↓

ProductResponse

---

## Method Reference

Instead of

```java
.map(product -> productMapper.toResponse(product))
```

Use

```java
.map(productMapper::toResponse)
```

Cleaner and more readable.

---

## Records

Purpose:

Immutable DTO.

Example:

```java
public record ProductResponse(...)
```

Benefits:

- Less boilerplate
- Immutable
- Automatically creates constructor, getters, equals(), hashCode(), toString()