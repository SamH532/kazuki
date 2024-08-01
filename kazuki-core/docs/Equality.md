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

The simplest form of customization is to add a comparable type limit.
By adding a `@ComparableTypeLimit` annotation to a module you indicate that it is only comparable with other instances of that type.

If an ancestor does not have an explicit comparable type limit than the basic type described in the previous section forms the limit.

For instance, if we have:
```

@ComparableTypeLimit
@Module
interface IntRecord3 {
    val f: Int
    val b: String
}

@Module
interface IntRecord4: IntRecord3


@ComparableTypeLimit
@Module
interface IntRecord5: IntRecord4

val i3 = mk_IntRecord3(8, "a")
val i4 = mk_IntRecord4(8, "a")
val i5 = mk_IntRecord5(8, "a")

```

Then the comparable type limit:
* for `IntRecord1` and `IntRecord2` is `Tuple2`
* for `IntRecord3` and `IntRecord4` is `IntRecord3`
* for `IntRecord5` is `IntRecord3`

And so:
* `i3 == i4` as they have the same comparable type limit
* `i3 != i2` and `i3 != i1` as they do not have the same comparable type limit
* `i3 != i5` and `i4 != i5` as they do not have the same comparable type limit

Note:
* Defining a comparable type limit will replace any inherited comparable type limit.
* It is forbidden for a type to inherit comparable type limit from unrelated ancestors; if the scenario arises it is likely that the joining module should be marked as new comparable type limit.

### Module specific relation

If you wish to define an equality relation specific to a module, you can define a property to use for comparison that
will replace the default.
This is done by adding a `@ComparableProperty` annotation to a property of a module that will be used to make an alternative instance for comparison.

For example, the following module defines a zone based time that resolves the time difference when determining equality :

```
@Module
interface Time {
    val h: Int
    val m: Int
    val s: Int
    val z: Zone

    @Invariant
    fun validHour() = h in 0..23

    @Invariant
    fun validMinute() = m in 0..59

    @Invariant
    fun validSecond() = s in 0..59

    @ComparableProperty
    val asUtc get() = mk_((h - z.offset) % 24, m, s)

    enum class Zone(val offset: Int) {
        GMT(0),
        CET(1),
        EST(-5)
    }
}
```

Note:

* Defining a property for comparison will automatically limit comparisons to the type as only instances of the type will
  have the required property.
* Defining a property for comparison on a type will replace any inherited comparison property.
* It is forbidden for a type to inherit comparison properties from unrelated ancestors; in this instance it will be
  necessary to define a property that explicitly resolves the ambiguity.
* The instance provided by the property is also used to generate the hashcode of an instance. This is done to ensure
  consistency, such that two equal instances will always have the same hashcode, which is a requirement of the JVM (upon
  which Kazuki executes).  

