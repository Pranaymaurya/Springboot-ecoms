# Current Data Model

This file records the model fields and mappings found in source. It does not redesign the schema. Where an ORM mapping is incomplete, that is called out as an observed implementation gap rather than replaced with an assumed schema.

## Persisted entities

| Entity | Source | Fields observed | Mapping |
|---|---|---|---|
| `Product` | `catalog/src/main/java/com/ecommerce/catalog/model/Product.java` | `Id: Long`, `name: String`, `desc: String`, `price: int` | `@Entity`; `Id` has `@GeneratedValue(IDENTITY)` but no `@Id` annotation. No explicit table or column names. |
| `User` | `user/src/main/java/com/ecommerce/user/model/User.java` | `id: Long`, `name: String`, `email: String`, `password: String`, `addresses: List<Address>` | `@Entity`, table name `users`; generated ID. One-to-many mapped by `Address.user`, cascade all, orphan removal. |
| `Address` | `user/src/main/java/com/ecommerce/user/model/Address.java` | `id: Long`, `street: String`, `locality: String`, `pincode: String`, `user: User` | `@Entity`; generated ID. Many-to-one with non-null `user_id` join column. |
| `Inventory` | `inventory/src/main/java/com/ecommerce/inventory/model/Inventory.java` | `Id: Long`, `quantity: Long`, `Reserved: Long` | `@Entity`; `Id` has `@GeneratedValue(IDENTITY)` but no `@Id`. No product identifier or explicit relationship appears in the class. |
| `Reservation` | `inventory/src/main/java/com/ecommerce/inventory/model/Reservation.java` | `Id: Long`, `order_id: Long`, `product_id: Long`, `quantity: int`, `status: boolean` | `@Entity`; `Id` has `@GeneratedValue(IDENTITY)` but no `@Id`. IDs are scalar fields; no entity relationships are declared. |
| `Order` | `orders/src/main/java/com/ecommerce/orders/model/Order.java` | `id: Long`, `total: Double`, `order_item: Order_item`, `user_id: Long` | `@Entity`; generated ID but no `@Id`. `order_item` is singular and annotated `@OneToMany(mappedBy = "order")`. |
| `Order_item` | `orders/src/main/java/com/ecommerce/orders/model/Order_item.java` | `Id: Long`, `order: Order`, `quantity: int`, `product_id: Long`, `price: Double` | `@Entity`; generated ID but no `@Id`. `order` has `@JoinColumn(name = "order_id")` but no `@ManyToOne`. No explicit table/column names otherwise. |

## Relationships

- `User` → `Address`: one-to-many, inverse field `Address.user`, cascade all, orphan removal enabled. `Address.user` is the owning many-to-one side and `user_id` is non-null.
- `Order` ↔ `Order_item`: intent appears to be a relationship, but mapping is inconsistent as written. The `Order` collection side is declared as a singular `Order_item`; the item side has a join-column annotation without a relationship annotation. Confirm intended cardinality and correct before using the model.
- `Inventory`/`Reservation` refer to product/order through scalar ID fields. No JPA relationship mappings are present.

## Schema and transaction evidence

- User's datasource is configured in `user/src/main/resources/application.properties` to a PostgreSQL database named `ecommerce_user`; credentials are intentionally not reproduced here.
- User JPA has `ddl-auto=update`, SQL logging, SQL formatting, and PostgreSQL dialect properties configured.
- No datasource URL is present in the catalog, inventory, orders, or payment properties files. POM dependencies alone do not establish a database connection.
- `AddressService` is class-level `@Transactional`; its get/list methods are `readOnly = true`. UserService and ProductService have no explicit service-level transaction annotation. Spring Data repository calls provide their own repository transaction behavior; no multi-step service transaction is explicitly defined.
- No database migration scripts or schema files were found in the inspected source structure.

## Key entities

1. **User:** account/profile fields plus owned addresses; mapped to the explicitly named `users` table.
2. **Address:** address fields associated to one user by a required `user_id`.
3. **Product:** catalog name, description, and integer price fields.
4. **Inventory:** quantity and reserved quantity fields.
5. **Reservation:** order/product identifiers, quantity, and status flag.
6. **Order:** total, user identifier, and a declared association to order item.
7. **Order_item:** order reference, product identifier, quantity, and price.

## Mapping caveats

All of `Product`, `Inventory`, `Reservation`, `Order`, and `Order_item` use `@GeneratedValue` without a visible `@Id`. JPA requires an identifier mapping; validate and correct these classes before treating generated schemas or runtime persistence as working. The model reflects code fields as written, including inconsistent capitalization such as `Id` and `Reserved`.
