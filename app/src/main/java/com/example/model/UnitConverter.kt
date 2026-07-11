package com.example.model

enum class MeasurementCategory(val displayName: String, val iconEmoji: String) {
    LENGTH("Length", "📏"),
    WEIGHT("Weight", "⚖️"),
    TEMPERATURE("Temperature", "🌡️"),
    VOLUME("Volume", "🧪")
}

interface MeasurementUnit {
    val displayName: String
    val symbol: String
}

enum class LengthUnit(override val displayName: String, override val symbol: String, val factorInMeters: Double) : MeasurementUnit {
    MILLIMETER("Millimeter", "mm", 0.001),
    CENTIMETER("Centimeter", "cm", 0.01),
    METER("Meter", "m", 1.0),
    KILOMETER("Kilometer", "km", 1000.0),
    INCH("Inch", "in", 0.0254),
    FOOT("Foot", "ft", 0.3048),
    YARD("Yard", "yd", 0.9144),
    MILE("Mile", "mi", 1609.344)
}

enum class WeightUnit(override val displayName: String, override val symbol: String, val factorInGrams: Double) : MeasurementUnit {
    MILLIGRAM("Milligram", "mg", 0.001),
    GRAM("Gram", "g", 1.0),
    KILOGRAM("Kilogram", "kg", 1000.0),
    OUNCE("Ounce", "oz", 28.349523125),
    POUND("Pound", "lb", 453.59237)
}

enum class TemperatureUnit(override val displayName: String, override val symbol: String) : MeasurementUnit {
    CELSIUS("Celsius", "°C"),
    FAHRENHEIT("Fahrenheit", "°F"),
    KELVIN("Kelvin", "K")
}

enum class VolumeUnit(override val displayName: String, override val symbol: String, val factorInLiters: Double) : MeasurementUnit {
    MILLILITER("Milliliter", "mL", 0.001),
    LITER("Liter", "L", 1.0),
    FLUID_OUNCE("Fluid Ounce", "fl oz", 0.0295735295625),
    CUP("Cup", "cup", 0.2365882365),
    GALLON("Gallon", "gal", 3.785411784)
}

object Converter {
    fun convert(value: Double, from: MeasurementUnit, to: MeasurementUnit, category: MeasurementCategory): Double {
        return try {
            when (category) {
                MeasurementCategory.LENGTH -> {
                    val fromLen = from as LengthUnit
                    val toLen = to as LengthUnit
                    (value * fromLen.factorInMeters) / toLen.factorInMeters
                }
                MeasurementCategory.WEIGHT -> {
                    val fromW = from as WeightUnit
                    val toW = to as WeightUnit
                    (value * fromW.factorInGrams) / toW.factorInGrams
                }
                MeasurementCategory.TEMPERATURE -> {
                    val fromTemp = from as TemperatureUnit
                    val toTemp = to as TemperatureUnit
                    convertTemperature(value, fromTemp, toTemp)
                }
                MeasurementCategory.VOLUME -> {
                    val fromVol = from as VolumeUnit
                    val toVol = to as VolumeUnit
                    (value * fromVol.factorInLiters) / toVol.factorInLiters
                }
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun convertTemperature(value: Double, from: TemperatureUnit, to: TemperatureUnit): Double {
        val celsius = when (from) {
            TemperatureUnit.CELSIUS -> value
            TemperatureUnit.FAHRENHEIT -> (value - 32) * 5.0 / 9.0
            TemperatureUnit.KELVIN -> value - 273.15
        }
        return when (to) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> celsius * 9.0 / 5.0 + 32.0
            TemperatureUnit.KELVIN -> celsius + 273.15
        }
    }
}
