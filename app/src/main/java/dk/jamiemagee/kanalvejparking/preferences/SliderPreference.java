package dk.jamiemagee.kanalvejparking.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SliderPreference extends ListPreference implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private SeekBar seekbar;
    private TextView splashtext, valuetext;
    private final Context context;

    private final String dialogmessage;


    public SliderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        // Get string value for dialogMessage :
        int dialogmessageid = attrs.getAttributeResourceValue(androidns, "dialogMessage", 0);
        if  (dialogmessageid == 0)
            dialogmessage = attrs.getAttributeValue(androidns, "dialogMessage");
        else dialogmessage = context.getString (dialogmessageid);
    }

    @Override
    protected View onCreateDialogView() {

        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout (context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        splashtext = new TextView (context);
        splashtext.setPadding(30, 10, 30, 10);
        if  (dialogmessage != null)
            splashtext.setText (dialogmessage);
        layout.addView (splashtext);

        valuetext = new TextView (context);
        valuetext.setGravity(Gravity.CENTER_HORIZONTAL);
        valuetext.setTextSize(32);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView (valuetext, params);

        seekbar = new SeekBar (context);
        seekbar.setOnSeekBarChangeListener(this);
        layout.addView (seekbar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setProgressBarValue();

        return layout;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // do not call super
    }

    private void setProgressBarValue() {
        String value = null;
        if (shouldPersist()) {
            value = getValue();
        }

        final int max = this.getEntries().length - 1;

        seekbar.setMax(max);
        seekbar.setProgress(this.findIndexOfValue (value));
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        setProgressBarValue();
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        final CharSequence textToDisplay = getEntryFromValue(value);
        valuetext.setText(textToDisplay);
    }

    private CharSequence getEntryFromValue(int value) {
        CharSequence[] entries = getEntries();
        return value >= 0 && entries != null ? entries[value] : null;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void showDialog(Bundle state) {
        super.showDialog(state);

        Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (shouldPersist()) {
            final int progressChoice = seekbar.getProgress();
            setValueIndex(progressChoice);
        }

        getDialog().dismiss();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        setSummary(value);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(getEntry());
    }
}