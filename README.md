# life.

*it's your life.*

## Overview

"life." is a deeply personalized, AI-powered lifestyle app that helps users manage their physical, mental, emotional, and spiritual well-being — all in one place. Unlike superficial wellness apps, life. provides a minimalist, real-time, fully customizable experience with a strong backend (Supabase), real AI coaching (Deepseek), anonymous social engagement, and total data integrity.

## Key Features

### 🏋️ Workouts
- Customizable workout plans (user-made or AI-suggested)
- Track workout completion daily
- Each workout updates daily streak
- Saved in Supabase with timestamp, type, notes
- Workout difficulty filters
- Daily suggestions from Deepseek based on energy/time/mood

### 🏃 Running
- Log runs manually or via GPS
- Track distance, pace, date, time
- Each run updates daily streak
- Stored in Supabase instantly
- Optional notes on each run

### 🥗 Diet / Nutrition
- Manual Meal Logging (Breakfast, Lunch, Dinner, Snacks)
- Water intake tracking
- Deepseek AI-generated meal plans based on dietary preference
- Calories/macros optional
- All meals saved to Supabase per user per day
- Suggested meal templates for offline mode

### ✨ Motivation
- "Motivational Line of the Day" (via Deepseek AI or curated database)
- Personalized motivational feedback
- Stored and updated daily in Supabase per user
- Prompt options like: "Struggling with X today" to get custom motivation

### 📓 Journaling
- Freeform text journals
- Prompt-based journaling via Deepseek
- Voice-to-text optional
- All entries saved immediately in Supabase
- Searchable by mood, tags, and day
- Encrypted option for private thoughts

### 🙏 Religion
- Support for Islam ☪, Christianity ✝, Judaism ✡
- Each user selects their faith in settings
- Faith-specific features (prayer tracking, verses, etc.)
- All logs are synced to Supabase per user account and update streaks

### 🔁 Streak System
- One global streak counter
- Increases +1 per day if any meaningful activity is logged
- Dies/reset to 0 if no activity is done for 24h
- Streak is saved and updated in real-time in Supabase

### 📒 Notes & Life Plans
- Free-form notes (any topic)
- Structured "Plan Builder" (create plans/goals)
- Tagging system: Work, Personal, Fitness, Spiritual, etc.
- Saved in Supabase and fully editable

### 📡 Shares (Anonymous Community Feed)
- Users can post short text updates, struggles, wins, thoughts
- No names, no profiles — truly anonymous
- Anyone can like ❤️ or comment 💬 anonymously
- Feed is public but only shows text (no media)

### 🧠 AI Coach (Deepseek API)
- Personalized daily plan generator
- Mood-based activity suggestions
- Journal prompt generation
- Workout & meal recommendation engine
- Motivational message generator

## Technical Stack

- **Frontend**: Kotlin with Jetpack Compose
- **Backend**: Supabase (PostgreSQL, Authentication, Storage)
- **AI Integration**: Deepseek API
- **Local Storage**: Room Database
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt
- **Asynchronous Programming**: Coroutines and Flow

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 17 or newer
- Supabase account
- Deepseek API key

### Setup

1. Clone the repository
2. Create a `.env.local` file based on `.env.local.example`
3. Add your Supabase URL, anon key, and Deepseek API key
4. Run the app in Android Studio

## Project Structure

- `src/main/kotlin/com/life/app/` - Main source directory
  - `data/` - Data layer (models, repositories, local and remote data sources)
  - `ui/` - UI layer (screens, components, viewmodels)
  - `di/` - Dependency injection modules
  - `util/` - Utility classes and extensions

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Supabase for the backend infrastructure
- Deepseek for the AI capabilities
- Jetpack Compose for the modern UI toolkit
