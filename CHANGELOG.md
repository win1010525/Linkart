### What's New

* Fixed linker not dropping when a minecart is destroyed.
* Fixed minecarts only dropping chains after unlinking due to large distances.
* Fixed a bug that allowed parent and child minecars to be double-linked.
* Fixed `getOtherEntities` in `adjustMovementForCollisions` being called for every entity instead of just minecarts.
* Every method in the `LinkableMinecart` interface is now `default`.
* `linkart$getLinkItem` should no longer return `null`.
* Added an experimental option to change distance between carts. `distance` in the config.