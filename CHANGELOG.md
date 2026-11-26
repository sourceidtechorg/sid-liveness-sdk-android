# Changelog

## 1.9.0

Full UI customisation, shared with iOS 1.9.0 (see CONTRACT.md).

- AWS Amplify liveness upgraded from 1.6.0 to 1.13.0, with Amplify 2.42.0, Kotlin 2.2.21 and Compose BOM 2026.06.00.
- `LivenessUIConfig` gains `showStartPage`, `hideCancelButton`, `colors` (`LivenessColors`) and `fonts` (`LivenessFonts`). Existing fields and constructor calls keep working.
- Colours and fonts style the AWS detector too, through the Material theme it reads.
- The config reaches `LivenessActivity` as one Serializable extra. The old per-field extras are still read.
- All SourceID text moved to string resources (`sid_liveness_*`), so apps can replace or translate it.
- The start page warns about the flashing colours (`sid_liveness_start_guideline_photosensitivity`).
- Behaviour aligned with iOS: the title and "Powered by" text use the primary colour, and the default primary is black (light) or white (dark).
- Fix: a missing `primaryColorHex` no longer becomes `#000000`, which made the dark theme's button black on black.
- New `CONTRACT.md`: the cross-platform reference for config fields, results and error codes.
