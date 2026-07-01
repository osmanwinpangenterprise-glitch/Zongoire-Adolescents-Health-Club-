package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Zongoire Youth Health Club Premium Palette ---
val PrimaryEmerald = Color(0xFF15803D)         // Elegant deep emerald green
val PrimaryEmeraldDark = Color(0xFF14532D)     // Very deep forest green
val PrimaryEmeraldVeryDark = Color(0xFF0F2F1D) // Highest contrast forest-black text green
val PrimaryEmeraldMint = Color(0xFFE8F5E9)     // Light fresh mint/emerald tint

// Change Secondary Blue to a premium, rich Satin Gold!
val SecondaryBlue = Color(0xFFC5A059)          // Satin brand Gold
val SecondaryBlueDark = Color(0xFFB45309)      // Warm bronze/dark gold
val SecondaryBlueLight = Color(0xFFFEF3C7)     // Soft light gold background

val AccentOrange = Color(0xFFD97706)           // Rich warm amber gold
val AccentOrangeLight = Color(0xFFFFFBEB)      // Soft gold light tint

val SuccessGreen = Color(0xFF16A34A)
val WarningYellow = Color(0xFFEAB308)
val WarningYellowLight = Color(0xFFFEF9C3)
val ErrorRed = Color(0xFFDC2626)
val ErrorRedLight = Color(0xFFFEF2F2)

val SlateBackground = Color(0xFFFAF9F6)        // Pure Alabaster Off-White (luxurious premium paper feel)
val SlateCardBg = Color(0xFFFFFFFF)            // Pure crisp white
val SlateTextDark = Color(0xFF1F2937)          // Crisp dark charcoal for text
val SlateTextMuted = Color(0xFF4B5563)         // Soft slate gray for secondary labels
val SlateBorder = Color(0xFFE5E7EB)            // Clean light structural border

// Map existing reference names to keep MainScreen.kt compiling flawlessly
val EditorialTeal = PrimaryEmerald
val EditorialTealDark = PrimaryEmeraldDark
val EditorialTealVeryDark = PrimaryEmeraldVeryDark
val EditorialMint = PrimaryEmeraldMint
val EditorialSoftGreenBg = Color(0xFFF0FDF4)
val EditorialBorderGreen = Color(0xFFBBF7D0)
val EditorialBorderGray = SlateBorder
val EditorialBg = SlateBackground
val EditorialText = SlateTextDark
val EditorialSubtext = SlateTextMuted

// Badge colors mapped to premium palette
val BlueBadgeBg = SecondaryBlueLight
val BlueBadgeText = Color(0xFF78350F)          // Deep bronze text for excellent contrast on gold
val PurpleBadgeBg = Color(0xFFFDF2F8)          // Elegant soft rose/gold complementary badge
val PurpleBadgeText = Color(0xFF9D174D)        // Deep rose text
val RedBadgeBg = ErrorRedLight
val RedBadgeText = Color(0xFF991B1B)

// Original Light Theme Mapping
val TealPrimary = PrimaryEmerald
val TealSecondary = SecondaryBlue
val MintAccent = PrimaryEmeraldMint
val LightBg = SlateBackground
val SoftSurface = SlateCardBg
val CoralOrange = AccentOrange

// Dark Theme Mapping (Editorial Dark variant matching premium green and gold)
val TealDarkPrimary = Color(0xFF4ADE80)        // Emerald light
val TealDarkSecondary = Color(0xFFFBBF24)      // Gold light
val DarkBg = Color(0xFF06150D)                 // Deep luxury forest green/black
val DarkSurface = Color(0xFF0D281B)            // Rich deep emerald surface card

