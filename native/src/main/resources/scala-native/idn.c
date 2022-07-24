#include <unicode/uidna.h>

// annoying glue layer ...
//
// By default icu4c mangles method names with the version of the library (h/t
// @keynmol). This glue layer exposes them to SN under a stable name.
// Fortunately we are not shipping binaries, so the actual version used will be
// determined in userland at linktime when these sources are compiled on their
// machine against a particular uidna.h.
//
// Further reading:
// https://unicode-org.github.io/icu/userguide/icu/design.html#icu-binary-compatibility

int32_t ip4s_uidna_IDNToASCII(const UChar *src, int32_t srcLength, UChar *dest,
                              int32_t destCapacity, int32_t options,
                              UParseError *parseError, UErrorCode *status) {
  return uidna_IDNToASCII(src, srcLength, dest, destCapacity, options,
                          parseError, status);
}

int32_t ip4s_uidna_IDNToUnicode(const UChar *src, int32_t srcLength,
                                UChar *dest, int32_t destCapacity,
                                int32_t options, UParseError *parseError,
                                UErrorCode *status) {
  return uidna_IDNToUnicode(src, srcLength, dest, destCapacity, options,
                            parseError, status);
}
