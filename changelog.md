# Persistent Bits Changelog (1.14)

## 2.0.3

***This is the final release of Persistent Bits in 1.14.4 unless there is some major bug. For new features please upgrade to 1.15.2+*** 😀

### Added

- Mini Loader! Loads only one chunk, waterloggable, requires a solid block below. Crafts 9 out of 1 full-size loader, and doesn't conflict if chunk loading overlap occurs.

## 2.0.1

### Fixed

* Chunk Loader not dropping anything 🤔

## 2.0.0

### Changed

* Chunk Loaders are no longer tile entities
  * Using a capability to keep track of the number of Chunk Loaders in a chunk
  * This capability lets me determine when one is added or removed
    * When the first one in the chunk is added, force loading is enabled
    * When the last one in the chunk is broken, force loading is stopped
* `PersistentBits.dat` isn't a thing anymore - using Forge World Capabilities now
* Using vanilla Force Load system instead of Forge Tickets (completely native solution 🙂)
* Loaded chunk "indicator" now uses particles instead of broken client-only visualization blocks

### Fixed

(From 1.12.x and below, at least)

* Weirdness with chunk loading tickets not actually working how they're supposed to
* Any issues with the `PersistentBits.dat` file when the game/world crashes
