package com.example;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private Spinner spinnerCategory;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private EditText editInputValue;
    private TextView txtSourceSymbol;
    private View cardResult;
    private TextView txtResultValue;
    private TextView txtResultUnit;
    private MaterialButton btnCopyResult;
    private MaterialButton btnConvertTactile;
    private FrameLayout btnSwapUnits;
    private FrameLayout btnSettings;
    private LinearLayout layoutHistoryContainer;
    private LinearLayout layoutHistoryList;

    // Categories
    private static final String CATEGORY_LENGTH = "Length 📏";
    private static final String CATEGORY_WEIGHT = "Weight ⚖️";
    private static final String CATEGORY_TEMPERATURE = "Temperature 🌡️";

    private final String[] categories = {CATEGORY_LENGTH, CATEGORY_WEIGHT, CATEGORY_TEMPERATURE};

    // Units lists
    private final String[] lengthUnits = {"Meters", "Centimeters", "Kilometers", "Inches", "Feet", "Miles"};
    private final String[] weightUnits = {"Kilograms", "Grams", "Pounds", "Ounces"};
    private final String[] tempUnits = {"Celsius", "Fahrenheit", "Kelvin"};

    // Base multipliers or formulas
    // Length (Base unit: Meter)
    private static final double MILES_TO_M = 1609.344;
    private static final double FEET_TO_M = 0.3048;
    private static final double INCHES_TO_M = 0.0254;
    private static final double KM_TO_M = 1000.0;
    private static final double CM_TO_M = 0.01;

    // Weight (Base unit: Kilogram)
    private static final double GRAM_TO_KG = 0.001;
    private static final double LB_TO_KG = 0.45359237;
    private static final double OZ_TO_KG = 0.028349523125;

    // History list to keep track of recent conversions (max 5)
    private final List<String> conversionHistory = new ArrayList<>();

    // Recent unit pairs list (max 3)
    private LinearLayout layoutRecentConversionsCard;
    private LinearLayout layoutRecentChips;
    private final List<String> recentConversionsList = new ArrayList<>();

    // Formatting for decimal outputs
    private final DecimalFormat df = new DecimalFormat("#,##0.0000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind Views
        initializeViews();

        // Setup Spinners
        setupCategorySpinner();

        // Setup Listeners
        setupListeners();

        // Load Recent Conversions and History
        loadRecentConversions();
        loadHistory();
    }

    private void initializeViews() {
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        editInputValue = findViewById(R.id.edit_input_value);
        txtSourceSymbol = findViewById(R.id.txt_source_symbol);
        cardResult = findViewById(R.id.card_result);
        txtResultValue = findViewById(R.id.txt_result_value);
        txtResultUnit = findViewById(R.id.txt_result_unit);
        btnCopyResult = findViewById(R.id.btn_copy_result);
        btnConvertTactile = findViewById(R.id.btn_convert_tactile);
        btnSwapUnits = findViewById(R.id.btn_swap_units);
        btnSettings = findViewById(R.id.settings_badge);
        layoutHistoryContainer = findViewById(R.id.layout_history_container);
        layoutHistoryList = findViewById(R.id.layout_history_list);
        layoutRecentConversionsCard = findViewById(R.id.layout_recent_conversions_card);
        layoutRecentChips = findViewById(R.id.layout_recent_chips);
    }

    private void setupCategorySpinner() {
        CustomSpinnerAdapter categoryAdapter = new CustomSpinnerAdapter(
                this,
                R.layout.spinner_item,
                categories,
                spinnerCategory
        );
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        // Category change listener
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                populateUnitSpinners(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Source and target unit spinners change listeners to update source symbol/live preview
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSourceSymbol();
                performLiveConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performLiveConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Live text change listener for the input field
        editInputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performLiveConversion();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Convert button click
        btnConvertTactile.setOnClickListener(v -> performManualConversion());

        // Swap button click
        btnSwapUnits.setOnClickListener(v -> swapUnits());

        // Copy button click
        btnCopyResult.setOnClickListener(v -> copyResultToClipboard());

        // Settings click
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        // Add subtle pressing animations to buttons
        applyTouchFeedback(btnConvertTactile);
        applyTouchFeedback(btnSwapUnits);
        applyTouchFeedback(btnCopyResult);
        applyTouchFeedback(btnSettings);
    }

    private void populateUnitSpinners(String category) {
        String[] units;
        if (category.equals(CATEGORY_LENGTH)) {
            units = lengthUnits;
        } else if (category.equals(CATEGORY_WEIGHT)) {
            units = weightUnits;
        } else {
            units = tempUnits;
        }

        CustomSpinnerAdapter fromAdapter = new CustomSpinnerAdapter(
                this,
                R.layout.spinner_item,
                units,
                spinnerFrom
        );
        CustomSpinnerAdapter toAdapter = new CustomSpinnerAdapter(
                this,
                R.layout.spinner_item,
                units,
                spinnerTo
        );

        spinnerFrom.setAdapter(fromAdapter);
        spinnerTo.setAdapter(toAdapter);

        // Pre-select second item for "To" unit so they are not the same initially
        if (units.length > 1) {
            spinnerTo.setSelection(1);
        }

        updateSourceSymbol();
    }

    private void updateSourceSymbol() {
        if (spinnerFrom.getSelectedItem() != null) {
            String selectedUnit = spinnerFrom.getSelectedItem().toString();
            txtSourceSymbol.setText(getUnitSymbol(selectedUnit));
        }
    }

    private String getUnitSymbol(String unitName) {
        switch (unitName) {
            case "Meters": return "m";
            case "Centimeters": return "cm";
            case "Kilometers": return "km";
            case "Inches": return "in";
            case "Feet": return "ft";
            case "Miles": return "mi";
            case "Kilograms": return "kg";
            case "Grams": return "g";
            case "Pounds": return "lb";
            case "Ounces": return "oz";
            case "Celsius": return "°C";
            case "Fahrenheit": return "°F";
            case "Kelvin": return "K";
            default: return "";
        }
    }

    private void swapUnits() {
        if (spinnerFrom.getSelectedItem() != null && spinnerTo.getSelectedItem() != null) {
            int fromPos = spinnerFrom.getSelectedItemPosition();
            int toPos = spinnerTo.getSelectedItemPosition();

            spinnerFrom.setSelection(toPos);
            spinnerTo.setSelection(fromPos);

            // Re-run conversion immediately on swap
            performLiveConversion();
        }
    }

    private void performLiveConversion() {
        String inputText = editInputValue.getText().toString().trim();
        if (inputText.isEmpty()) {
            cardResult.setVisibility(View.GONE);
            return;
        }

        try {
            double inputValue = Double.parseDouble(inputText);
            String fromUnit = spinnerFrom.getSelectedItem().toString();
            String toUnit = spinnerTo.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            double result = convertValue(category, inputValue, fromUnit, toUnit);

            // Format result
            String formattedResult = formatValue(result);

            txtResultValue.setText(formattedResult);
            txtResultUnit.setText(toUnit + " (" + getUnitSymbol(toUnit) + ")");
            
            // Elegant slide & fade-in for live conversions
            if (cardResult.getVisibility() != View.VISIBLE) {
                cardResult.setVisibility(View.VISIBLE);
                cardResult.setAlpha(0f);
                cardResult.setTranslationY(15f);
                cardResult.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(250)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }

        } catch (NumberFormatException e) {
            cardResult.setVisibility(View.GONE);
        }
    }

    private void performManualConversion() {
        String inputText = editInputValue.getText().toString().trim();
        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            cardResult.setVisibility(View.GONE);
            return;
        }

        try {
            double inputValue = Double.parseDouble(inputText);
            String fromUnit = spinnerFrom.getSelectedItem().toString();
            String toUnit = spinnerTo.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            double result = convertValue(category, inputValue, fromUnit, toUnit);
            String formattedResult = formatValue(result);

            // Update UI
            txtResultValue.setText(formattedResult);
            txtResultUnit.setText(toUnit + " (" + getUnitSymbol(toUnit) + ")");
            
            // Success animation feedback!
            if (cardResult.getVisibility() != View.VISIBLE) {
                cardResult.setVisibility(View.VISIBLE);
                cardResult.setAlpha(0f);
                cardResult.setTranslationY(20f);
                cardResult.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            } else {
                // Subtle tactile scale bounce/pulse when result is updated
                cardResult.animate()
                        .scaleX(1.03f)
                        .scaleY(1.03f)
                        .setDuration(120)
                        .withEndAction(() -> cardResult.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start())
                        .start();
            }

            // Add to History
            String historyText = inputText + " " + getUnitSymbol(fromUnit) + " = " + formattedResult + " " + getUnitSymbol(toUnit);
            addToHistory(historyText);

            // Add to Recent Conversions (Quick Access)
            addRecentConversion(category, fromUnit, toUnit);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            cardResult.setVisibility(View.GONE);
        }
    }

    private double convertValue(String category, double value, String from, String to) {
        if (from.equals(to)) {
            return value;
        }

        if (category.equals(CATEGORY_LENGTH)) {
            return convertLength(value, from, to);
        } else if (category.equals(CATEGORY_WEIGHT)) {
            return convertWeight(value, from, to);
        } else {
            return convertTemperature(value, from, to);
        }
    }

    private double convertLength(double value, String from, String to) {
        // First convert to base unit: Meters
        double meters;
        switch (from) {
            case "Meters": meters = value; break;
            case "Centimeters": meters = value * CM_TO_M; break;
            case "Kilometers": meters = value * KM_TO_M; break;
            case "Inches": meters = value * INCHES_TO_M; break;
            case "Feet": meters = value * FEET_TO_M; break;
            case "Miles": meters = value * MILES_TO_M; break;
            default: meters = 0; break;
        }

        // Convert from Meters to target unit
        switch (to) {
            case "Meters": return meters;
            case "Centimeters": return meters / CM_TO_M;
            case "Kilometers": return meters / KM_TO_M;
            case "Inches": return meters / INCHES_TO_M;
            case "Feet": return meters / FEET_TO_M;
            case "Miles": return meters / MILES_TO_M;
            default: return 0;
        }
    }

    private double convertWeight(double value, String from, String to) {
        // First convert to base unit: Kilograms
        double kgs;
        switch (from) {
            case "Kilograms": kgs = value; break;
            case "Grams": kgs = value * GRAM_TO_KG; break;
            case "Pounds": kgs = value * LB_TO_KG; break;
            case "Ounces": kgs = value * OZ_TO_KG; break;
            default: kgs = 0; break;
        }

        // Convert from Kilograms to target unit
        switch (to) {
            case "Kilograms": return kgs;
            case "Grams": return kgs / GRAM_TO_KG;
            case "Pounds": return kgs / LB_TO_KG;
            case "Ounces": return kgs / OZ_TO_KG;
            default: return 0;
        }
    }

    private double convertTemperature(double value, String from, String to) {
        // First convert to base unit: Celsius
        double celsius;
        switch (from) {
            case "Celsius": celsius = value; break;
            case "Fahrenheit": celsius = (value - 32) * 5.0 / 9.0; break;
            case "Kelvin": celsius = value - 273.15; break;
            default: celsius = 0; break;
        }

        // Convert from Celsius to target unit
        switch (to) {
            case "Celsius": return celsius;
            case "Fahrenheit": return (celsius * 9.0 / 5.0) + 32;
            case "Kelvin": return celsius + 273.15;
            default: return 0;
        }
    }

    private String formatValue(double value) {
        // Avoid trailing zeros if they are exact, but format nicely
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        String formatted = df.format(value);
        // Trim redundant trailing fractional zeros
        if (formatted.contains(".")) {
            formatted = formatted.replaceAll("0+$", "");
            if (formatted.endsWith(".")) {
                formatted = formatted.substring(0, formatted.length() - 1);
            }
        }
        return formatted;
    }

    private void addToHistory(String item) {
        // Remove duplicate if it exists so it goes to the top
        conversionHistory.remove(item);
        conversionHistory.add(0, item);

        // Limit to max 5 items
        if (conversionHistory.size() > 5) {
            conversionHistory.remove(5);
        }

        saveHistory();
        updateHistoryUI();
    }

    private void loadHistory() {
        conversionHistory.clear();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String savedHistory = prefs.getString("conversion_history_data", "");
        if (!savedHistory.isEmpty()) {
            String[] items = savedHistory.split("##");
            for (String item : items) {
                if (!item.trim().isEmpty()) {
                    conversionHistory.add(item.trim());
                }
            }
        }
        updateHistoryUI();
    }

    private void saveHistory() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < conversionHistory.size(); i++) {
            sb.append(conversionHistory.get(i));
            if (i < conversionHistory.size() - 1) {
                sb.append("##");
            }
        }
        prefs.edit().putString("conversion_history_data", sb.toString()).apply();
    }

    private void updateHistoryUI() {
        if (conversionHistory.isEmpty()) {
            layoutHistoryContainer.setVisibility(View.GONE);
            return;
        }

        layoutHistoryContainer.setVisibility(View.VISIBLE);
        layoutHistoryList.removeAllViews();

        for (String historyText : conversionHistory) {
            TextView textView = new TextView(this);
            textView.setText(historyText);
            textView.setTextColor(getResources().getColor(R.color.bento_text_dark));
            textView.setTextSize(16);
            textView.setPadding(0, 10, 0, 10);
            
            // Add custom separator/divider view
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divider.setBackgroundColor(getResources().getColor(R.color.bento_secondary_container));

            layoutHistoryList.addView(textView);
            layoutHistoryList.addView(divider);
        }
    }

    private void copyResultToClipboard() {
        String valueText = txtResultValue.getText().toString();
        String unitText = txtResultUnit.getText().toString();
        String fullResult = valueText + " " + unitText;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Unit Converter Result", fullResult);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getResources().getString(R.string.msg_copied), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettingsDialog() {
        android.view.View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        
        RadioGroup themeGroup = dialogView.findViewById(R.id.theme_radio_group);
        RadioButton radioSystem = dialogView.findViewById(R.id.radio_system);
        RadioButton radioLight = dialogView.findViewById(R.id.radio_light);
        RadioButton radioDark = dialogView.findViewById(R.id.radio_dark);
        MaterialButton btnClearHistory = dialogView.findViewById(R.id.btn_clear_history);
        android.view.View btnCloseSettingsIcon = dialogView.findViewById(R.id.btn_close_settings_icon);

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        dialog.setContentView(dialogView);

        // Remove default bottom sheet background to preserve our custom rounded corners
        android.widget.FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
            // Configure BottomSheetBehavior to slide smoothly up to expanded state
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        // Set current checked state based on saved SharedPreferences setting
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int savedTheme = prefs.getInt("theme_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (savedTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
            radioDark.setChecked(true);
        } else if (savedTheme == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) {
            radioLight.setChecked(true);
        } else {
            radioSystem.setChecked(true);
        }

        // Theme Selection Change Listener
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int currentTheme = prefs.getInt("theme_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            int newThemeMode;
            String themeName;
            if (checkedId == R.id.radio_dark) {
                newThemeMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
                themeName = "Dark Theme";
            } else if (checkedId == R.id.radio_light) {
                newThemeMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                themeName = "Light Theme";
            } else {
                newThemeMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                themeName = "System Default Theme";
            }

            if (newThemeMode != currentTheme) {
                prefs.edit().putInt("theme_mode", newThemeMode).apply();
                Toast.makeText(MainActivity.this, "Theme updated to " + themeName, Toast.LENGTH_SHORT).show();

                // Dismiss dialog first to avoid window leak or visual glitch during recreation
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                // Apply theme mode directly - recreates activity cleanly
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(newThemeMode);
            }
        });

        // Clear History with Confirmation Dialog to protect destructive action
        btnClearHistory.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to delete your entire conversion history? This action cannot be undone.")
                    .setPositiveButton("Clear All", (dialogInterface, which) -> {
                        conversionHistory.clear();
                        saveHistory();
                        layoutHistoryList.removeAllViews();
                        layoutHistoryContainer.setVisibility(android.view.View.GONE);

                        // Clear Recent Conversions (Quick Access) as well
                        recentConversionsList.clear();
                        saveRecentConversions();
                        updateRecentConversionsUI();

                        Toast.makeText(MainActivity.this, "Conversion history cleared", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Close Dialog via Top-Right Close Button
        if (btnCloseSettingsIcon != null) {
            btnCloseSettingsIcon.setOnClickListener(v -> dialog.dismiss());
            applyTouchFeedback(btnCloseSettingsIcon);
        }

        dialog.show();
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private void applyTouchFeedback(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
                    break;
            }
            return false;
        });
    }

    private void loadRecentConversions() {
        recentConversionsList.clear();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String savedRecents = prefs.getString("recent_conversions_data", "");
        if (!savedRecents.isEmpty()) {
            String[] items = savedRecents.split("##");
            for (String item : items) {
                if (!item.trim().isEmpty()) {
                    recentConversionsList.add(item.trim());
                }
            }
        }
        updateRecentConversionsUI();
    }

    private void saveRecentConversions() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentConversionsList.size(); i++) {
            sb.append(recentConversionsList.get(i));
            if (i < recentConversionsList.size() - 1) {
                sb.append("##");
            }
        }
        prefs.edit().putString("recent_conversions_data", sb.toString()).apply();
    }

    private void addRecentConversion(String category, String fromUnit, String toUnit) {
        String entry = category + ";" + fromUnit + ";" + toUnit;

        // Remove duplicate if exists so it moves to top
        recentConversionsList.remove(entry);
        recentConversionsList.add(0, entry);

        // Limit to max 3
        if (recentConversionsList.size() > 3) {
            recentConversionsList.remove(3);
        }

        saveRecentConversions();
        updateRecentConversionsUI();
    }

    private void updateRecentConversionsUI() {
        if (recentConversionsList.isEmpty()) {
            layoutRecentConversionsCard.setVisibility(View.GONE);
            return;
        }

        layoutRecentConversionsCard.setVisibility(View.VISIBLE);
        layoutRecentChips.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (String entry : recentConversionsList) {
            String[] parts = entry.split(";");
            if (parts.length < 3) continue;

            final String category = parts[0];
            final String fromUnit = parts[1];
            final String toUnit = parts[2];

            View chipView = inflater.inflate(R.layout.item_recent_chip, layoutRecentChips, false);
            TextView txtLabel = chipView.findViewById(R.id.txt_chip_label);

            txtLabel.setText(fromUnit + " ➔ " + toUnit);

            chipView.setOnClickListener(v -> {
                selectCategoryAndUnits(category, fromUnit, toUnit);
            });

            applyTouchFeedback(chipView);
            layoutRecentChips.addView(chipView);
        }
    }

    private void selectCategoryAndUnits(String category, String fromUnit, String toUnit) {
        int categoryIndex = -1;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                categoryIndex = i;
                break;
            }
        }

        if (categoryIndex != -1) {
            spinnerCategory.setSelection(categoryIndex);

            final String fUnit = fromUnit;
            final String tUnit = toUnit;
            spinnerCategory.post(() -> {
                if (spinnerFrom.getAdapter() != null) {
                    for (int j = 0; j < spinnerFrom.getAdapter().getCount(); j++) {
                        if (spinnerFrom.getAdapter().getItem(j).toString().equals(fUnit)) {
                            spinnerFrom.setSelection(j);
                            break;
                        }
                    }
                }

                if (spinnerTo.getAdapter() != null) {
                    for (int j = 0; j < spinnerTo.getAdapter().getCount(); j++) {
                        if (spinnerTo.getAdapter().getItem(j).toString().equals(tUnit)) {
                            spinnerTo.setSelection(j);
                            break;
                        }
                    }
                }

                performLiveConversion();
            });
        }
    }

    // Custom ArrayAdapter that dynamically styles the list items and highlights the currently selected option
    private class CustomSpinnerAdapter extends ArrayAdapter<String> {
        private final Spinner associateSpinner;

        public CustomSpinnerAdapter(android.content.Context context, int resource, String[] objects, Spinner spinner) {
            super(context, resource, objects);
            this.associateSpinner = spinner;
            setDropDownViewResource(R.layout.spinner_dropdown_item);
        }

        @Override
        public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                if (position == associateSpinner.getSelectedItemPosition()) {
                    tv.setBackgroundColor(getContext().getResources().getColor(R.color.bento_soft_highlight));
                    tv.setTextColor(getContext().getResources().getColor(R.color.bento_primary));
                    tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                } else {
                    tv.setBackgroundColor(getContext().getResources().getColor(R.color.bento_white));
                    tv.setTextColor(getContext().getResources().getColor(R.color.bento_text_dark));
                    tv.setTypeface(android.graphics.Typeface.DEFAULT);
                }
            }
            return view;
        }
    }
}
