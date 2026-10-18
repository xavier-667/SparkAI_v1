# SparkAI — Native Android AI Assistant Architecture & UI/UX Design

SparkAI is a premier native Android AI assistant built exclusively with Kotlin and Jetpack Compose, delivering multi-turn chat, multimodal reasoning (vision, camera, files, audio), creative studios (image generation, Veo video, Lyria music), live voice, coding workspaces, and deep research grounded in Search and Maps, backed by hybrid Firestore synchronization and Google Sign-In.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Summary of Confirmed Design & Architecture Decisions (Phase 1):**
> - **Brand Identity & Name**: **SparkAI** — An original, futuristic visual identity featuring a luminous midnight obsidian canvas with electric cyan, celestial violet, and neon aura accents. No Google trademarks, proprietary logos, or copied assets are used.
> - **Navigation Model**: **Adaptive Modal Navigation Drawer with Bottom Prompt Composer & Quick-Switcher** — Instant one-tap access across all 20 specialized workspaces, paired with an ergonomic persistent bottom composer that adapts dynamically to text, voice, camera, and multimodal attachment modes. On tablets and foldables, seamlessly transitions into a split-pane Navigation Rail.
> - **Data Persistence & Sync**: **Hybrid Firestore Cloud Sync with Google Sign-In** — End-to-end user isolation (`/users/{uid}/...`), zero-trust Firestore security rules, automated server timestamps, and seamless offline caching.

---

## 1. Overview & Core Concept

SparkAI provides a unified, fluid Android experience that elevates everyday problem-solving, deep research, coding, creative generation, and hands-free voice interactions into a single native workspace.

- **Target Persona**: Power users, developers, researchers, students, and creative professionals who need immediate, high-intelligence assistance without switching between disjointed apps.
- **Key Value**: Native Android performance, edge-to-edge Material 3 visual polish, true low-latency streaming responses, multi-model intelligence routing, and robust multi-device synchronization.

---

## 2. Visual Identity & Design System

### 2.1 Theme & Aesthetic Direction

SparkAI embodies an **"Atmospheric Deep Space & Neon Aurora"** aesthetic: deep midnight obsidian backgrounds that save OLED battery, illuminated by subtle soft-focus radial aura glows and crisp, luminous typography.

#### Dark Theme Palette (Default)
| Token | Hex Value | Semantic Usage |
|---|---|---|
| `Primary` | `#38BDF8` (Sky Cyan) | Primary actions, user bubble accents, active indicators |
| `OnPrimary` | `#082F49` | Text/icons on primary surfaces |
| `Secondary` | `#A855F7` (Electric Violet) | AI thinking highlights, creative studio accents |
| `Tertiary` | `#F43F5E` (Rose Quartz) | Recording states, camera triggers, live audio pulse |
| `Background` | `#0B0F19` (Obsidian Navy) | App background canvas |
| `Surface` | `#111827` (Deep Slate) | Cards, bottom sheets, elevated containers |
| `SurfaceVariant` | `#1E293B` (Tonal Slate) | Secondary buttons, prompt input field, message cards |
| `SurfaceContainer`| `#161F30` | Top bars, dialogs, drawer panels |
| `Outline` | `#334155` | Subtle borders, dividers, chip boundaries |
| `AuraGlow` | `#6366F1` (Indigo glow) | Ambient radial gradients behind hero greetings |

#### Light Theme Palette (Optional toggle)
| Token | Hex Value | Semantic Usage |
|---|---|---|
| `Primary` | `#0284C7` (Cobalt Blue) | Primary actions, branding |
| `Secondary` | `#9333EA` (Purple Iris) | Model badges, creative studio buttons |
| `Background` | `#F8FAFC` (Pure Ice) | Main background |
| `Surface` | `#FFFFFF` | Elevated cards, conversation bubbles |
| `SurfaceVariant` | `#F1F5F9` | Input fields, inactive chips |
| `Outline` | `#E2E8F0` | Card borders, dividers |

### 2.2 Typography Hierarchy
- **Brand Display & Hero Headings**: Bold geometric font styling (`headlineLarge` 32sp, `headlineMedium` 28sp).
- **Section Headers & Titles**: Semi-bold, tight letter spacing (`titleLarge` 22sp, `titleMedium` 16sp).
- **Body & Chat Stream**: High-readability neutral grotesque typeface (`bodyLarge` 16sp, line height 24sp; `bodyMedium` 14sp).
- **Code & Technical Output**: Monospace Jetpack Compose typeface (`FontFamily.Monospace`, 13sp) inside syntax-highlighted rounded containers with line numbering and one-tap copy.

---

## 3. Screen Inventory & Phone Frame Previews

The 20 dedicated screens are previewed below inside realistic Android frames, demonstrating the exact UI layout, hierarchy, and affordances.

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [01] Splash & Onboarding             │  │ [02] Main Home Screen & Composer     │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│                                      │  │ [≡] SparkAI            [Pro] [Avatar]│
│               ✦                      │  │ ──────────────────────────────────── │
│            SparkAI                   │  │                                      │
│        Intelligence Unleashed        │  │  Good morning, Alex ✦                │
│                                      │  │  What would you like to explore?     │
│   ┌──────────────────────────────┐   │  │                                      │
│   │ [✦] Multimodal Intelligence  │   │  │  ┌──────────┐ ┌──────────┐ ┌───────┐ │
│   │ [⚡] Real-time Live Voice     │   │  │  │ Deep     │ │ Code     │ │ Image │ │
│   │ [🛡] Zero-Knowledge Privacy   │   │  │  │ Research │ │ Studio   │ │ Gen   │ │
│   └──────────────────────────────┘   │  │  └──────────┘ └──────────┘ └───────┘ │
│                                      │  │                                      │
│   [  Continue with Google (G)   ]    │  │  Recent Sparks:                      │
│   [        Explore as Guest      ]   │  │  • Quantum Computing Paradigms  (2h) │
│                                      │  │  • Kotlin Compose Multiplatform (1d) │
│   By continuing you accept our Terms │  │  ┌─────────────────────────────────┐ │
│                                      │  │  │ ✦ Ask SparkAI anything...       │ │
│                                      │  │  │ [+] [📷] [🎤]               [➤] │ │
│         ─── Gesture Bar ───          │  │  └─────────────────────────────────┘ │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [03] Navigation Drawer & History     │  │ [04] Active Streaming Conversation   │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [x] SparkAI Menu      [New Chat +]   │  │ [←] Quantum Algorithms       [⋮]     │
│ ──────────────────────────────────── │  │ Model: Gemini 4 Argon (Thinking)   │
│ Workspaces:                          │  │ ──────────────────────────────────── │
│ ✦ Home Dashboard                     │  │ [User Bubble]                        │
│ 🔍 Deep Research Hub                 │  │ Explain Shor's algorithm simply.     │
│ 🎨 Creative Studios (Image/Video)   │  │                                      │
│ 🎙 Live Voice Studio                 │  │ [SparkAI Thinking: 4.2s] [▼ Detail]  │
│ 💻 Code Assistant & Canvas           │  │ ✦ SparkAI                            │
│ 📚 Study Hub & Quizzes               │  │ Shor's algorithm is a quantum        │
│ 📁 Projects & Memory                 │  │ polynomial-time algorithm for prime  │
│ ⚙ Settings & Diagnostics            │  │ factorization... ▌ (streaming)       │
│                                      │  │                                      │
│ Pinned Conversations:                │  │ [🔊 Speak] [📋 Copy] [🔄 Retry]      │
│ ⭐ Architecture Review 2026          │  │ ──────────────────────────────────── │
│ ⭐ Jetpack Compose Canvas Shader     │  │ ┌─────────────────────────────────┐ │
│                                      │  │ │ Ask a follow up...          [➤] │ │
│ Alex Mercer (alex@example.com) [SignOut]│ │ [📷] [📎] [🎤]                   │ │
│         ─── Gesture Bar ───          │  │         ─── Gesture Bar ───          │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [05] Model Selection & Capabilities  │  │ [06] Web Search & Source Citations   │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] Select AI Engine                 │  │ [←] Search Grounded Answer     [🔍]  │
│ ──────────────────────────────────── │  │ ──────────────────────────────────── │
│ ● Gemini 4 Argon                     │  │ Query: Latest Mars Sample Return     │
│   Supreme reasoning & High Thinking  │  │                                      │
│   Active for: Science, Math, Code    │  │ Grounded via Google Search [● Live] │
│                                      │  │ Sources:                             │
│ ○ Gemini 3.8 Flash                   │  │ [1] nasa.gov/perseverance-update     │
│   Everyday speed, tools & vision     │  │ [2] jpl.nasa.gov/news-2026           │
│                                      │  │                                      │
│ ○ Gemini 3.1 Flash-Lite              │  │ ✦ SparkAI:                           │
│   Ultra low-latency (<250ms)         │  │ NASA's updated Mars Sample Return    │
│                                      │  │ mission architecture [1] now         │
│ ○ Gemini 3.8 Live                    │  │ schedules sample retrieval in 2028   │
│   Real-time bidirectional voice/cam  │  │ using dual mini-helicopters [2].     │
│                                      │  │                                      │
│ Custom Parameters:                   │  │ [Explore Interactive Map Citation]   │
│ Temperature: 0.7  [━━━━●━━━━]        │  │ ┌─────────────────────────────────┐ │
│ Thinking Level: [High ▼]             │  │ │ Ask about these sources...  [➤] │ │
│         ─── Gesture Bar ───          │  │         ─── Gesture Bar ───          │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [07] Deep Research & Reports         │  │ [08] Image Understanding & Creation  │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] Deep Research Hub         [Export│  │ [←] Visual Studio         [Gallery]  │
│ ──────────────────────────────────── │  │ ──────────────────────────────────── │
│ Report: Solid-State Battery Tech     │  │ Mode: [Create Image] [Analyze Photo] │
│ Status: Completed (14 Sources, 8m)   │  │                                      │
│                                      │  │ Prompt: "Cyberpunk rooftop garden,   │
│ Executive Summary:                   │  │ holographic rain, cinematic lighting"│
│ Solid-state electrolyte densities    │  │                                      │
│ have exceeded 500 Wh/kg in 2026...   │  │ Aspect Ratio: [1:1] [16:9] [9:16] [▼]│
│                                      │  │ Quality: [1K] [2K (HD)] [4K (Pro)]   │
│ Key Findings:                        │  │ Engine: gemini-3-pro-image-preview   │
│ 1. Anode-free lithium metal designs  │  │                                      │
│ 2. Scaled manufacturing cost curve   │  │ ┌──────────────────────────────────┐ │
│                                      │  │ │ [Generated 4K Artwork Preview]   │ │
│ [📊 View Interactive Comparison Chart]│ │ │ [⬇ Save] [✏ Edit Prompt] [🎬 Veo] │ │
│ [📄 Export as PDF / Markdown]        │  │ └──────────────────────────────────┘ │
│         ─── Gesture Bar ───          │  │         ─── Gesture Bar ───          │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [09] Voice Conversation & Live Pulse │  │ [10] Live Camera & Multimodal Vision │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] Live Voice Studio        [Headset│  │ [←] Live Camera Assistant      [Flash│
│                                      │  │ ┌──────────────────────────────────┐ │
│                                      │  │ │                                  │ │
│              (  ✦  )                 │  │ │     [ Camera Viewfinder:         │ │
│           (((     )))                │  │ │       Circuit board component    │ │
│        (((((       )))))             │  │ │       with highlighted ICs ]     │ │
│           (((     )))                │  │ │                                  │ │
│              (  ✦  )                 │  │ └──────────────────────────────────┘ │
│                                      │  │ Real-time Vision:                    │
│   "Listening... What's on your mind?"│  │ "I see a STM32 Microcontroller with │
│                                      │  │ an ungrounded bypass capacitor."     │
│ Voice: Kore (Warm Female) [Switch]   │  │                                      │
│ Latency: 180ms                       │  │ [Snap & Inspect]   [Live Continuous] │
│                                      │  │                                      │
│   [ Mute ]    [ End Call ]    [ Hold ]│ │ ┌─────────────────────────────────┐ │
│         ─── Gesture Bar ───          │  │ │ Ask: "Why is pin 4 shorted?"[➤]│ │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [11] File Attachments & Doc Analysis │  │ [12] Coding Assistant & Syntax View │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] Document Inspector       [Share] │  │ [←] Kotlin Coroutine Channel  [Run]  │
│ ──────────────────────────────────── │  │ Language: Kotlin | Gemini 4 Argon    │
│ Attached Files:                      │  │ ──────────────────────────────────── │
│ ┌──────────────────────────────────┐ │  │ [User] How to buffer flows safely?   │
│ │ [📄] Q3_Financial_Audit.pdf (4MB)│ │  │                                      │
│ │ 38 pages • Processed via Argon   │ │  │ ✦ SparkAI:                           │
│ └──────────────────────────────────┘ │  │ ┌──────────────────────────────────┐ │
│                                      │  │ │ 1 class EventBus {               │ │
│ Document Breakdown:                  │  │ │ 2   private val _ch = Channel<Ev>│ │
│ • Revenue Growth: +24% YoY           │  │ │ 3   val flow = _ch.receiveAsFlow │ │
│ • Operating Margin: 31.5%            │  │ │ 4 }                              │ │
│                                      │  │ └──────────────────────────────────┘ │
│ Recommended Queries:                 │  │ [📋 Copy Code]  [📂 Open in Canvas]  │
│ [Summarize Risks] [Extract Tables]   │  │                                      │
│ ┌──────────────────────────────────┐ │  │ ┌─────────────────────────────────┐ │
│ │ Ask questions about this PDF...[➤]│ │ │ │ Ask follow-up or test case... [➤]│ │
│ └──────────────────────────────────┘ │  │ └─────────────────────────────────┘ │
│         ─── Gesture Bar ───          │  │         ─── Gesture Bar ───          │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [13] Canvas Workspace & Editor       │  │ [14] Study Mode, Quizzes & Tutor     │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] Canvas: Mobile App Spec  [Export]│  │ [←] Study Mode: Organic Chemistry    │
│ ──────────────────────────────────── │  │ Progress: 8/10 Flashcards • Level 2  │
│ [Edit Text] [AI Diff Review] [History│  │ ──────────────────────────────────── │
│                                      │  │ Question 8 of 10:                    │
│ # SparkAI Implementation Plan        │  │ What is the major product of an      │
│ - Architecture: Clean MVVM           │  │ SN2 reaction with an inverted center?│
│ - Security: Zero-trust rules         │  │                                      │
│                                      │  │ [A] Racemic mixture                  │
│ [SparkAI Inline Suggestion]:         │  │ [B] Walden inversion enantiomer  [✓] │
│ "Add automated timeout backoff for   │  │ [C] Retention of configuration       │
│ websocket reconnections."            │  │                                      │
│ [Accept ✓]  [Reject ✗]  [Refine...] │  │ Explanation:                         │
│                                      │  │ Backside attack leads to 100%        │
│ Word Count: 1,420 • Version 3        │  │ stereochemical inversion.            │
│         ─── Gesture Bar ───          │  │ [Next Card →]       [Ask AI Tutor]   │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [15] Projects, Memory & Context      │  │ [16] Tools & Integrations Hub        │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] Projects & Memory        [New +] │  │ [←] Tools & Function Calling   [Add] │
│ ──────────────────────────────────── │  │ ──────────────────────────────────── │
│ Active Projects:                     │  │ Enabled Capabilities:                │
│ ┌──────────────────────────────────┐ │  │ [✓] Google Search Grounding          │
│ │ 📁 Android Kernel Porting        │ │  │     Real-time web indexing           │
│ │ 12 chats • 4 documents attached  │ │  │                                      │
│ └──────────────────────────────────┘ │  │ [✓] Google Maps Grounding            │
│ ┌──────────────────────────────────┐ │  │     Live geographic locations        │
│ │ 📁 Master Thesis: Robotics       │ │  │                                      │
│ │ 34 chats • 9 code snippets       │ │  │ [✓] Python Code Execution Sandbox    │
│ └──────────────────────────────────┘ │  │     Runs mathematical algorithms     │
│                                      │  │                                      │
│ Long-Term Contextual Memory:         │  │ [✓] Veo Video Generation (Veo 3.1)   │
│ • Preferred Language: Kotlin 2.2     │  │ [✓] Lyria Music Studio (Lyria 3)     │
│ • Writing Style: Concise, Technical  │  │ [✓] Live Camera Vision Interpreter   │
│ [Manage Memory Items] [Clear All]    │  │                                      │
│         ─── Gesture Bar ───          │  │ Configure Custom OpenAPI Endpoints > │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [17] Universal Conversation Search   │  │ [18] Settings, Appearance & Privacy  │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] [ 🔍 Search chats, code, files ] │  │ [←] Settings                         │
│ Filters: [All] [Code] [Images] [Docs]│  │ ──────────────────────────────────── │
│ ──────────────────────────────────── │  │ Appearance:                          │
│ Matching Results (3 found):          │  │ Theme: [ Midnight Dark (OLED) ▼ ]    │
│                                      │  │ Accent Color: [ Sky Cyan & Violet ]  │
│ ✦ "Coroutine channel buffer..."      │  │ Font Scale: Default (100%)           │
│   in Kotlin Coroutine Channel • 2d   │  │                                      │
│                                      │  │ Privacy & Security:                  │
│ ✦ "Solid-state battery report"       │  │ • Cloud Sync: Enabled (Firestore)    │
│   in Deep Research Hub • Oct 14      │  │ • Biometric App Lock: [ On / Off ]   │
│                                      │  │ • Telemetry & Diagnostics: Off       │
│ ✦ "Circuit board schematic inspection│  │                                      │
│   in Live Camera Assistant • 5d      │  │ App Version: SparkAI v1.0.0          │
│ [Clear Recent Search History]        │  │ Google Account: alex@example.com     │
│         ─── Gesture Bar ───          │  │         ─── Gesture Bar ───          │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

```
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│ [19] API & Diagnostics Console       │  │ [20] System & Edge Case States       │
├──────────────────────────────────────┤  ├──────────────────────────────────────┤
│ 10:00                5G  [■■■]  98%  │  │ 10:00                5G  [■■■]  98%  │
│ [←] Connection Diagnostics    [Ping] │  │ [←] State Showcase                   │
│ ──────────────────────────────────── │  │ ──────────────────────────────────── │
│ Gemini Gateway:                      │  │ [Offline Mode Card]                  │
│ Endpoint: generativelanguage.google  │  │ ⚡ No Internet Connection             │
│ Key Source: BuildConfig (Injected)   │  │ Local cached chats remain readable.  │
│ Status: [ Healthy • 42ms ]           │  │                                      │
│                                      │  │ [Rate Limit (429) Banner]            │
│ Firebase Services:                   │  │ ⚠️ Model quota reached. Falling back │
│ Firestore Database: Connected        │  │ to Gemini 3.1 Flash-Lite in 12s...   │
│ Auth State: Authenticated (alex)     │  │                                      │
│ App Check Token: Valid               │  │ [Permission Denied State]            │
│                                      │  │ 📷 Camera permission required for    │
│ Model Health Matrix:                 │  │ live inspection. [Grant in Settings] │
│ • gemini-4-argon: ONLINE             │  │                                      │
│ • gemini-3.8-flash: ONLINE           │  │ [Empty State]                        │
│ • veo-3.1-fast: READY                │  │ ✦ Start a fresh exploration          │
│ • lyria-3-pro: READY                 │  │ Tap a suggested prompt above to begin│
│         ─── Gesture Bar ───          │  │         ─── Gesture Bar ───          │
└──────────────────────────────────────┘  └──────────────────────────────────────┘
```

---

## 4. Reusable Material 3 Components & Architecture

### 4.1 Reusable UI Primitives
1. `SparkTopBar`: Centered or small M3 top app bar with navigation drawer trigger, workspace status, active model pill, and user profile avatar.
2. `SparkComposer`: Fluid floating/docked bottom bar supporting text input, mic activation, camera capture, file picker, and quick capability toggle chips.
3. `MessageBubble`: Dynamic bubble supporting user prompts, AI text streaming with blinking cursor, expandable "Thinking Process" card, code blocks with syntax coloring and copy actions, and interactive citations.
4. `ModelSelectorSheet`: M3 modal bottom sheet allowing users to switch dynamically between Gemini 4 Argon, 3.8 Flash, 3.1 Flash-Lite, and Live models.
5. `MediaAttachmentPreview`: Horizontal scrollable thumbnail carousel for selected images, PDFs, audio snippets, or code files before prompting.
6. `InteractiveStateBanner`: Unobtrusive top/bottom pill notifying users of offline status, rate-limit countdowns, or background generation progress.

### 4.2 Comprehensive Navigation Architecture

SparkAI uses **Jetpack Navigation Compose** backed by type-safe route keys:

```
                          ┌───────────────────────────┐
                          │    Splash & Onboarding    │
                          └─────────────┬─────────────┘
                                        │
                            (Google Sign-In / Auth)
                                        │
                                        ▼
    ┌───────────────────────────────────────────────────────────────────────┐
    │                        Main Home Dashboard                            │
    │  - AI Greeting, Quick Suggestions, Pinned Sessions, SparkComposer     │
    └──────┬──────────────────────┬──────────────────────────┬──────────────┘
           │                      │                          │
           ▼                      ▼                          ▼
┌────────────────────┐ ┌────────────────────┐      ┌────────────────────┐
│ Modal Drawer /     │ │ Live Chat Screen   │      │ Creative Studios   │
│ Workspace Switcher │ │ - Streaming Stream │      │ - Image Studio     │
│ - 20 Feature Views │ │ - Thinking Tray    │      │ - Veo Video Studio │
│ - Project Folders  │ │ - Code & Citations │      │ - Lyria Music Lab  │
└────────────────────┘ └────────────────────┘      └────────────────────┘
           │                      │                          │
           ▼                      ▼                          ▼
┌────────────────────┐ ┌────────────────────┐      ┌────────────────────┐
│ Research & Canvas  │ │ Voice & Vision     │      │ System Management  │
│ - Deep Research    │ │ - Live Voice Call  │      │ - Settings & Theme │
│ - Document Reader  │ │ - Camera Inspector │      │ - API Diagnostics  │
│ - Code Canvas      │ │ - Study Flashcards │      │ - Global Search    │
└────────────────────┘ └────────────────────┘      └────────────────────┘
```

---

## 5. Screen Inventory & Kotlin File Mapping

| # | Screen Description | Kotlin File Path | Key ViewModel / State |
|---|---|---|---|
| 01 | Splash & Onboarding | `ui/screens/onboarding/OnboardingScreen.kt` | `AuthViewModel` |
| 02 | Main Home Dashboard | `ui/screens/home/HomeScreen.kt` | `HomeViewModel` |
| 03 | Nav Drawer & History | `ui/components/navigation/SparkNavDrawer.kt` | `NavigationViewModel` |
| 04 | AI Streaming Chat | `ui/screens/chat/ChatScreen.kt` | `ChatViewModel` |
| 05 | Model Selection Sheet | `ui/screens/model/ModelSelectionSheet.kt` | `ModelConfigViewModel` |
| 06 | Search & Citations | `ui/screens/search/SearchGroundedScreen.kt` | `ChatViewModel` |
| 07 | Deep Research Reports | `ui/screens/research/DeepResearchScreen.kt` | `ResearchViewModel` |
| 08 | Image Generation/Vision | `ui/screens/creative/ImageStudioScreen.kt` | `ImageStudioViewModel` |
| 09 | Live Voice Call | `ui/screens/voice/LiveVoiceScreen.kt` | `VoiceViewModel` |
| 10 | Camera Vision Inspector | `ui/screens/camera/CameraVisionScreen.kt` | `CameraVisionViewModel` |
| 11 | Doc Analysis & Attachments | `ui/screens/documents/DocumentScreen.kt` | `DocumentViewModel` |
| 12 | Code Assistant & Highlight | `ui/screens/code/CodeAssistantScreen.kt` | `CodeAssistantViewModel` |
| 13 | Canvas Workspace | `ui/screens/canvas/CanvasWorkspaceScreen.kt` | `CanvasViewModel` |
| 14 | Study Mode & Quizzes | `ui/screens/study/StudyHubScreen.kt` | `StudyViewModel` |
| 15 | Projects & Memory | `ui/screens/projects/ProjectsScreen.kt` | `ProjectsViewModel` |
| 16 | Tools & Integrations | `ui/screens/tools/ToolsScreen.kt` | `ToolsViewModel` |
| 17 | Global Chat Search | `ui/screens/search/UniversalSearchScreen.kt`| `SearchViewModel` |
| 18 | Settings & Privacy | `ui/screens/settings/SettingsScreen.kt` | `SettingsViewModel` |
| 19 | API Diagnostics Console | `ui/screens/diagnostics/DiagnosticsScreen.kt` | `DiagnosticsViewModel` |
| 20 | Error, Empty & Offline | `ui/components/feedback/StateFeedbackView.kt` | Shared across all Views |

---

## 6. Technical Architecture, Persistence & Service Dependencies

### 6.1 System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  (M3 ColorScheme, Custom Animations, Haptics, Insets & Window Sizing)  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Unidirectional Data Flow (UDF)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         ViewModel Layer (StateFlow)                    │
│   ChatViewModel   •   ResearchViewModel   •   CreativeStudioViewModel  │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
                    ▼                                ▼
┌───────────────────────────────────────┐ ┌──────────────────────────────┐
│        AI & Networking Engine         │ │    Persistence & Storage     │
│  - Gemini REST & SSE Streaming Client │ │  - Firebase Firestore        │
│  - Gemini 4 Argon / 3.8 Flash         │ │    (/users/{uid}/chats)      │
│  - Veo 3.1 & Lyria 3 Generation       │ │    (/users/{uid}/projects)   │
│  - OkHttp 60s Timeout Client          │ │  - Credential Manager Auth   │
│  - BuildConfig.GEMINI_API_KEY Injected│ │  - Room Local Cache          │
└───────────────────────────────────────┘ └──────────────────────────────┘
```

### 6.2 Model Routing Matrix
- **Deep Reasoning, Complex Science & High Thinking**: `gemini-4-argon` with `thinkingLevel = "HIGH"`.
- **General Multimodal, Search & Maps Grounding**: `gemini-3.8-flash` with Google Search & Google Maps tools.
- **Low-Latency & Rapid Text**: `gemini-3.1-flash-lite`.
- **Live Bidirectional Voice**: `gemini-3.8-live` (with fallback to `gemini-2.5-flash-native-audio-preview-12-2025`).
- **High-Fidelity Image Generation**: `gemini-3-pro-image-preview` (1K, 2K, 4K) & `gemini-nano-banana-2.1`.
- **Video Generation from Text & Images**: `veo-3.1-fast-generate-preview` (16:9 & 9:16).
- **Music & Audio Tracks**: `lyria-3-clip-preview` (<=30s) and `lyria-3-pro-preview`.
- **Text-to-Speech**: `gemini-3.8-flash-tts`.
- **Microphone Audio Transcription**: `gemini-3.5-transcribe`.

### 6.3 Required Hardware Permissions & External Services
- `android.permission.INTERNET` (Normal): REST, SSE streaming, and Firestore synchronization.
- `android.permission.ACCESS_NETWORK_STATE` (Normal): Connectivity tracking for offline states.
- `android.permission.RECORD_AUDIO` (Runtime): For live voice conversations and audio transcription.
- `android.permission.CAMERA` (Runtime): For live camera inspection and visual Q&A.
- `ActivityResultContracts.PickVisualMedia` (Zero-permission photo picker): Android 13+ compliant photo selection without requiring legacy storage permissions.
- `Firebase Firestore & Auth`: Cloud database and Google Sign-In identity provider.

---

## 7. Next Steps: Phase 2 Implementation Roadmap

Upon your review and approval of this architectural plan and screen specification, Phase 2 will execute sequentially:

1. **Brand Assets & Theme Setup**: Configure `strings.xml`, `metadata.json`, and the M3 color/typography system (`Color.kt`, `Theme.kt`, `Type.kt`).
2. **Core AI & Networking Layer**: Implement Retrofit SSE streaming service, Gemini model routing, request builders (Thinking, Grounding, ImageConfig, Veo, Lyria), and secure `BuildConfig` injection.
3. **Firebase & Local Data Layer**: Scaffold Firestore schema, `firestore.rules`, and repository implementations for multi-turn chats, projects, and memory.
4. **All 20 Screens & Navigation Scaffold**: Implement the complete adaptive drawer navigation, persistent composer, and individual high-fidelity screen composables with full animations and state handling.
5. **Compilation & Verification**: Verify clean build via `compile_applet` and execute local tests.
