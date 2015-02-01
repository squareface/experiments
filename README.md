# experiments
Random useful bits of code

Package com.squareface.intern
Contains a thread-safe, generic InternDomain using Cliff Click's NonBlockingHashMap to store the interned entries. All keys/values are stored as WeakReferences to ensure automatic cleanup when no longer referenced. A single InternDomain is required for each type.
Interner is just a factory for creating/returning an InternDomain for a given class.
Tests need to be beefed up with microbenchmarking
