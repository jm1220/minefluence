# Billboard Upload Mapping

Billboard uploads are resolved server-side from mission index, route, and posting
style. The posting and reward flow is unchanged.

## Upload Files

The 28 mission upload PNG files are packaged under
`src/main/resources/assets/minefluence/textures/billboard/`:

- `mission_1_good_normally.png`
- `mission_1_good_exaggerated.png`
- `mission_1_bad_normally.png`
- `mission_1_bad_exaggerated.png`
- `mission_2_good_normally.png`
- `mission_2_good_exaggerated.png`
- `mission_2_bad_normally.png`
- `mission_2_bad_exaggerated.png`
- `mission_3_good_normally.png`
- `mission_3_good_exaggerated.png`
- `mission_3_bad_normally.png`
- `mission_3_bad_exaggerated.png`
- `mission_4_good_normally.png`
- `mission_4_good_exaggerated.png`
- `mission_4_bad_normally.png`
- `mission_4_bad_exaggerated.png`
- `mission_5_good_normally.png`
- `mission_5_good_exaggerated.png`
- `mission_5_bad_normally.png`
- `mission_5_bad_exaggerated.png`
- `mission_6_good_normally.png`
- `mission_6_good_exaggerated.png`
- `mission_6_bad_normally.png`
- `mission_6_bad_exaggerated.png`
- `mission_7_good_normally.png`
- `mission_7_good_exaggerated.png`
- `mission_7_bad_normally.png`
- `mission_7_bad_exaggerated.png`

`default.png` remains the non-mission fallback. The older generic aliases
`mission_1.png` through `mission_7.png` are still valid for manual billboard
commands, but automatic uploads do not use them.

## Resolution Rule

`MineFluenceBillboardImageResolver.resolveUploadedMissionImage(...)` maps upload
state directly to:

`mission_<missionNumber>_<good|bad>_<normally|exaggerated>`

Examples:

- Mission 1 Good normal -> `mission_1_good_normally.png`
- Mission 1 Good exaggerated -> `mission_1_good_exaggerated.png`
- Mission 1 Bad normal -> `mission_1_bad_normally.png`
- Mission 1 Bad exaggerated -> `mission_1_bad_exaggerated.png`

The resolver no longer uses cross-mission semantic fallback entries. This keeps
mission number, Good/Bad route, and Normal/Exaggerated posting style from being
swapped accidentally.

## Missing Artwork

When an expected upload image is missing from the packaged resources, the
resolver logs the mission number, route, posting style, and expected path once
for that combination, then falls back to `textures/billboard/default.png`.

The client renderer also falls back to `default.png` if a manually selected
billboard image is not present in the loaded resource pack.
