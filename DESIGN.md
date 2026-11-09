---
name: PHISHWARE
description: A reassuring campus safety console for checking links and learning to recognize phishing.
colors:
  brand-wine: "#790D2B"
  brand-hover: "#5F0A22"
  brand-active: "#4D071B"
  brand-raspberry: "#B42345"
  rose-wash: "#F8E9EE"
  canvas: "#F7F7FA"
  surface: "#FFFFFF"
  ink: "#17171C"
  muted-ink: "#686873"
  border: "#E7E4E8"
  safe-green: "#16845B"
  warning-amber: "#C78316"
  danger-red: "#C6283D"
  dark-canvas: "#020617"
  dark-surface: "#0F172A"
  dark-raised: "#1E293B"
typography:
  display:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "3rem"
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: "-0.025em"
  headline:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "2.25rem"
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: "-0.025em"
  title:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 700
    lineHeight: 1.3
    letterSpacing: "-0.025em"
  body:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 600
    lineHeight: 1.4
  badge:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 600
    lineHeight: 1.4
  mono:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.5
rounded:
  control: "12px"
  card: "16px"
  badge: "9999px"
spacing:
  xs: "4px"
  sm: "8px"
  control-gap: "12px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.brand-wine}"
    textColor: "#FFFFFF"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "10px 24px"
  button-primary-hover:
    backgroundColor: "{colors.brand-hover}"
    textColor: "#FFFFFF"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "10px 24px"
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "10px 24px"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.muted-ink}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "8px 16px"
  input-field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "12px 16px"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.card}"
    padding: "24px"
  badge-safe:
    backgroundColor: "#16845B1A"
    textColor: "{colors.safe-green}"
    typography: "{typography.badge}"
    rounded: "{rounded.badge}"
    padding: "4px 12px"
  badge-suspicious:
    backgroundColor: "#C783161A"
    textColor: "#A66E12"
    typography: "{typography.badge}"
    rounded: "{rounded.badge}"
    padding: "4px 12px"
  badge-dangerous:
    backgroundColor: "#C6283D1A"
    textColor: "#A71F31"
    typography: "{typography.badge}"
    rounded: "{rounded.badge}"
    padding: "4px 12px"
  nav-link-active:
    backgroundColor: "{colors.rose-wash}"
    textColor: "#5F0A22"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "10px 16px"
---

# Design System: PHISHWARE

## Overview

**Creative North Star: "The Campus Safety Console"**

PHISHWARE's web interface is a clear, reassuring, educational place to assess a link and decide what to do next. It pairs a practical dashboard and persistent navigation with small, legible status cues. The overall impression is capable and approachable, not alarmist: safe, suspicious, and dangerous states are distinct, sober messages rather than fear-driven decoration.

Inter and compact controls keep the interface direct and easy to scan. The wine-and-raspberry brand colors identify primary actions, while pale neutral surfaces, subtle borders, and restrained shadows carry most of the structure. The UI supports light and dark themes and a Spanish-first experience with English localization.

**Key Characteristics:**
- Reassuring, task-focused security guidance
- Burgundy brand accent with restrained semantic risk colors
- Rounded controls and layered neutral surfaces
- Persistent, responsive app navigation
- Light and dark themes; Spanish-first interface

## Colors

The palette centers on deep wine for primary actions, raspberry as a supporting accent, pale cool neutrals for the canvas, and reserved green, amber, and red for risk states.

### Primary
- **Deep Wine** (#790D2B): Primary actions and brand emphasis; hover shifts to the darker wine ramp.
- **Raspberry** (#B42345): Supporting brand accent and restrained emphasis.

### Secondary
- **Rose Wash** (#F8E9EE): Selected navigation and soft primary-tinted surfaces.

### Neutral
- **Pale Canvas** (#F7F7FA): Main light-theme page background.
- **White Surface** (#FFFFFF): Cards, fields, and light-theme navigation surfaces.
- **Near-Black Ink** (#17171C): Primary text.
- **Muted Gray** (#686873): Secondary text, hints, and quiet controls.
- **Pale Border** (#E7E4E8): Borders and dividers.
- **Midnight Canvas** (#020617): Dark-theme page background.
- **Midnight Surface** (#0F172A): Dark-theme content surfaces.
- **Raised Slate** (#1E293B): Dark-theme sidebar and elevated surface tone.

### Named Rules
**The Risk-Color Rule.** Reserve green, amber, and red for safe, suspicious, and dangerous states respectively; keep these indicators legible and sober rather than alarmist.

## Typography

**Display Font:** Inter (with system-ui, sans-serif fallback)  
**Body Font:** Inter (with system-ui, sans-serif fallback)  
**Label/Mono Font:** JetBrains Mono for URLs and technical strings.

**Character:** Inter gives headings and controls a compact, contemporary clarity. JetBrains Mono distinguishes literal URLs and technical values from ordinary interface copy.

### Hierarchy
- **Display** (700, 48px, 1.1 line-height, -0.025em): Landing-page hero heading on wide screens.
- **Headline** (700, 36px, 1.1 line-height, -0.025em): Landing-page hero heading at smaller breakpoints.
- **Title** (700, 24px, 1.3 line-height, -0.025em): Section and page headings.
- **Body** (400, 16px, 1.5 line-height): General interface copy; larger 18px text is used for landing-page lead copy.
- **Label** (600, 14px, 1.4 line-height): Field labels, controls, and important compact labels; badges also use compact 12px text.
- **Mono** (400, 14px, 1.5 line-height): URLs and technical strings.

### Named Rules
**The Readable-Risk Rule.** Keep verdicts, explanations, and actionable labels in the sans-serif hierarchy; use monospace for literal URLs and technical values, not whole paragraphs.

## Layout

The web app uses a centered content column capped at 72rem, with 16px page padding by default, 24px from the `sm` breakpoint, and 32px from `lg`. A 16rem sidebar stays alongside the content on large screens and becomes an off-canvas drawer with an overlay on smaller screens. The top bar is 64px high. Landing content uses the same centered width, a two-column hero from `lg`, and a three-column feature grid from `md`.

Dashboard sections favor scan-friendly grids: metric cards progress from two columns to four at `xl`, while recent activity and supporting information collapse to one column on narrow screens. Common spacing follows the 4px utility rhythm, with 8px, 12px, 16px, 24px, and 32px used repeatedly.

## Elevation & Depth

Depth is layered rather than pronounced: surface tone and borders establish structure; a soft shadow gives cards just enough separation. Hover can strengthen the shadow with a restrained wine tint. Avoid stacking multiple heavy shadows or using elevation as the primary signal for risk.

### Shadow Vocabulary
- **Card Rest** (`0 1px 2px rgba(23,23,28,0.05), 0 8px 24px -12px rgba(23,23,28,0.12)`): Default `.card` surface.
- **Card Hover** (`0 2px 4px rgba(23,23,28,0.06), 0 12px 32px -12px rgba(121,13,43,0.18)`): Interactive `.card-hover` and stat-card hover state.

### Named Rules
**The Quiet-Layer Rule.** Use borders and tonal separation first; reserve the stronger shadow for interactive hover feedback.

## Shapes

The form language is softly rounded and consistent: cards use 16px corners, fields and controls use 12px corners, and status badges are pill-shaped. Borders are thin and low-contrast in neutral states. Inputs and cards retain visible boundaries in both themes; status badges combine a faint semantic tint with a matching border.

## Components

### Buttons
- **Character:** Clear, rounded, and reassuring: strong primary actions, quiet supporting controls, and legible fields.
- **Shape:** 12px radius.
- **Primary:** Deep-wine fill, white semibold text, 24px horizontal and 10px vertical padding.
- **Hover / Focus:** Primary darkens from #790D2B to #5F0A22; active state uses #4D071B. Keyboard focus uses a 2px wine outline/ring with a 2px offset.
- **Secondary:** White surface, ink text, and a pale neutral border; dark theme uses a dark surface.
- **Ghost / Danger:** Ghost controls use a transparent background and muted text until hover. Destructive actions use the danger-red scale and are reserved for destructive intent.

### Chips
- **Style:** Compact pill with a light semantic fill, semantic text, and a thin border.
- **State:** Safe is green, suspicious is amber, and dangerous is red. Pair the color with an icon and a localized text verdict.

### Cards / Containers
- **Corner Style:** 16px.
- **Background:** White in light theme; dark-surface in dark theme.
- **Shadow Strategy:** Use the subtle card-rest shadow; use the stronger wine-tinted shadow only on interactive hover cards.
- **Border:** Thin ink-200 in light theme and dark-800 in dark theme.
- **Internal Padding:** 24px by default; some preview/compact surfaces deliberately override it.

### Inputs / Fields
- **Style:** Full-width white surface, 1px neutral border, 12px radius, 16px horizontal and 12px vertical padding.
- **Focus:** 2px primary ring with a primary border; use a visible 2px focus-visible outline for keyboard navigation.
- **Error / Disabled:** Error fields switch to the danger-red border and ring. Disabled fields use a muted surface and text.

### Navigation
- **Style:** Sidebar links use 16px horizontal and 10px vertical padding, a 12px radius, and a compact Lucide icon with a text label.
- **Default / Hover:** Muted ink at rest; a neutral tint and stronger text on hover.
- **Active:** Rose-wash surface, dark-wine text, and a subtle wine border.
- **Mobile:** Sidebar becomes an off-canvas drawer behind a dismissible overlay; the top bar exposes the menu control.

### Risk Verdict
Use the shared `RiskBadge` treatment to make URL assessments scannable: pair a small state icon with localized text and the safe/suspicious/dangerous semantic color. Do not rely on color alone.

## Do's and Don'ts

### Do:
- **Do** use the existing wine brand ramp for primary actions and selected navigation.
- **Do** preserve the mapping of green to safe, amber to suspicious, and red to dangerous.
- **Do** retain light/dark theme behavior, Spanish-first copy, and the English locale.
- **Do** keep visible focus treatment and honor reduced-motion preferences.
- **Do** use JetBrains Mono for URLs and technical values.
- **Do** pair risk color with an icon and a text verdict.

### Don't:
- **Don't** use alarmist visuals or amplify fear around threat states.
- **Don't** use danger red for ordinary primary actions or decorative emphasis.
- **Don't** replace the established logo, shield identity, or wine brand palette without an explicit redesign.
- **Don't** make safe, suspicious, or dangerous status understandable by color alone.
- **Don't** add heavy shadows to every surface or make the dashboard denser than its existing responsive grids.
