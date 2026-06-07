# MineFluence Demo Script

1. Run `/minefluence demo setup`.
   - This resets the demo, selects Farmer, gives the starting Farmer hoe, and keeps configured areas intact.
   - If required areas are missing, the demo map preset is loaded automatically without overwriting existing manual areas.

2. Check readiness with `/minefluence demo check`.
   - Use `/minefluence area load_preset` to overwrite all four required areas with the fixed demo map preset.
   - Use `/minefluence area set_box <type> <x1> <y1> <z1> <x2> <y2> <z2>` for exact coordinates.
   - Use `/minefluence area list` to confirm all required areas show coordinates.
   - Use `/minefluence area show garden` or another area type to preview the particle boundary.

3. Start gameplay with `/minefluence mission next`.
   - Choose `/minefluence mission choose good` for the implemented good route.
   - Choose `/minefluence mission choose bad` to show the bad-route framework.
   - Complete the objective naturally when possible.
   - Use `/minefluence demo skip_mission` if the presenter needs recovery.

4. After a mission completes, choose a posting style:
   - `/minefluence post normal`
   - `/minefluence post exaggerate`

5. To advance quickly, use:
   - `/minefluence demo quick_normal`
   - `/minefluence demo quick_exaggerate`
   - `/minefluence demo quick_bad_normal`
   - `/minefluence demo quick_bad_exaggerate`

6. Show the first invasion after mission 2.
   - Fight the zombies normally, or recover with `/minefluence invasion stop_debug`.

7. Show weapon progression by gaining followers.
   - The sidebar and Farmer hoe tier update from Follower count.

8. Fast-forward to the ending if needed.
   - Use quick demo commands to finish missions.
   - Clear Invasion 3 to trigger the ending, or use `/minefluence ending trigger_debug` for a direct ending preview path.
