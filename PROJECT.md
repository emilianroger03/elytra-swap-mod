# Elytra Swap Mod

## What is it?
A Fabric mod that auto-swaps elytra/chestplate while flying to save durability — the "0.9 second swap" trick automated.

## Why am I building this?
Flying 1M blocks tonight and need zero durability loss on the elytra!

## How it works
1. Press **V** to toggle ON (while already flying with elytra)
2. Mod instantly swaps elytra to hotbar slot 8, chestplate goes on
3. Every 0.9 seconds: briefly equips elytra → presses space (re-engages flight) → swaps chestplate back
4. Elytra is only equipped for 2 ticks per cycle = virtually no durability loss
5. Press **V** again to toggle OFF

## Setup
- Put your chestplate in **hotbar slot 8**
- Start flying normally with elytra
- Press V to activate the swap cycle

## Steps
- [x] Step 1: Set up Fabric mod project
- [x] Step 2: Write swap logic with tick counter
- [x] Step 3: Build and install to mods folder
- [ ] Step 4: Test in-game and tune timing if needed

## Today's Goal
Get the mod working for the 1M block flight!

## Progress Log
- 2026-08-02: Created mod, built successfully, installed to mods folder
