# Equality in Kazuki

## General principles

Instances in Kazuki will generally be comparable with any other instance of the same basic type.

For example, given the following:

```
@Module
interface Over5Set: Set<Int> {
    @Invariant
    fun allOver5() = forall(this) { it > 5 }
}

@Module
interface Under10Set: Set<Int> {
    @Invariant
    fun under10() = forall(this) { it < 10 }
}

val o = mk_Over5Set(8, 7, 9)
val u = mk_Under10Set(9, 8, 7)

```

Then `o == u` as they both represent the same set.

The concept follows trivially for sequences and mappings.

Record types are considered to be tuples and so will be comparable with any other tuples. For example:

```
@Module
interface IntRecord1 {
    val f: Int
    val b: String
}

@Module
interface IntRecord2 {
    val g: Int
    val c: String
}

val i1 = mk_IntRecord1(8, "a")
val i2 = mk_IntRecord2(8, "a")

```

Then `i1 == i2` as they both represent the same tuple.

## Customizing

### Limit to type

TODO - Implement

TODO - Examples

### Module specific relation

If you wish to define an equality relation specific to a module, you can define a property to use for comparison that
will replace the default.

TODO - Examples

Note:

* Defining a property for comparison will automatically limit comparisons to the type as only instances of the type will
  have the required property.
* Defining a property for comparison on a type will replace any inherited comparison property.
* It is forbidden for a type to inherit comparison properties from unrelated ancestors; in this instance it will be
  necessary to define a property that explicitly resolves the ambiguity.
* The instance provided by the property is also used to generate the hashcode of an instance. This is done to ensure
  consistency, such that two equal instances will always have the same hashcode, which is a requirement of the JVM (upon
  which Kazuki executes).  

