package com.ibm.casesdk.sample.edittask.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ibm.casemanagersdk.sdk.interfaces.ICMProperty;
import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;
import com.ibm.casemanagersdk.sdk.interfaces.ICMLayout;
import com.ibm.casesdk.sample.edittask.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stefano on 29/04/2015.
 */
public class TaskDisplayHelper {

    public interface PropertyChangeListener {

        /**
         * Notify registered listeners when a property has a new value.
         *
         * @param property
         * @param newValue
         */
        void onPropertyChanged(final ICMProperty property, final String newValue);
    }

    private Context mContext;
    private HashMap<String, String> mUpdatedValues;
    private Calendar mCalendar;
    private LayoutInflater mInflater;
    private PropertyChangeListener mPropertyChangeListener;


    public TaskDisplayHelper(@NonNull Context context, PropertyChangeListener listener) {
        mContext = context;
        mUpdatedValues = new HashMap<>();
        mCalendar = Calendar.getInstance();
        mInflater = LayoutInflater.from(mContext);
        mPropertyChangeListener = listener;
    }

    /**
     * This method will create all the layouts required to display the properties of the given
     * {@link ICMTask}. The generated layouts will be added in the supplied {@link LinearLayout}
     * parameter. If the task is {@param isEditable} then we also generate appropriate editors based
     * on the {@link ICMProperty} type/
     *
     * @param task                -  the task that we want to display
     * @param propertiesContainer - parent layout in which all generated layouts will be added
     * @param isEditable          - flag that determines if the {@link ICMTask} is editable
     */
    public void displayTask(@NonNull ICMTask task, @NonNull LinearLayout propertiesContainer, boolean isEditable) {
        List<ICMLayout> layouts = null;

        // a task can have properties but no layout
        try {
            layouts = task.getLayout();
        } catch (Exception e) {
            // no action required
        }

        if (layouts == null || layouts.isEmpty()) {
            List<ICMProperty> propertyList = null;

            try {
                propertyList = task.getProperties();
            } catch (Exception e) {
                //
            }

            // if we have properties we display them
            if (propertyList != null && propertyList.size() > 0) {
                // display layout properties
                displayProperties(propertiesContainer, propertyList, isEditable);
            } else {
                // otherwise we inform the user that we didn't find any proeprty
                propertiesContainer.addView(mInflater.inflate(R.layout.layout_task_message, null, false));
            }

        } else {
            final List<ICMProperty> propertyList = task.getProperties();

            // for each layout
            for (ICMLayout layout : layouts) {
                final List<ICMProperty> layoutProperties = new ArrayList<>();

                // if this layout is a section - add a section header
                if (layout.getType().equalsIgnoreCase(ICMLayout.LAYOUT_SECTION)) {
                    final TextView sectionHeader = (TextView) mInflater.inflate(R.layout.layout_task_section, null, false);

                    // if we don't have a title the section will just say "Generic properties"
                    if (layout.getTitle() != null) {
                        sectionHeader.setText(layout.getTitle());
                    }

                    propertiesContainer.addView(sectionHeader);
                }

                // get the task properties associated with the current layout
                for (ICMProperty p : propertyList) {
                    final String symbolicName = p.getSymbolicName();
                    boolean isInLayout;
                    if (layout.getType().equalsIgnoreCase(ICMLayout.LAYOUT_SECTION)) {

                        // section layouts have property names defined in a list
                        isInLayout = !TextUtils.isEmpty(symbolicName) && layout.getProperties().contains(symbolicName);
                    } else {

                        // regular layouts define the property name as the layout name
                        isInLayout = !TextUtils.isEmpty(symbolicName) &&
                                !TextUtils.isEmpty(layout.getName()) &&
                                layout.getName().equalsIgnoreCase(symbolicName);
                    }

                    if (isInLayout) {
                        layoutProperties.add(p);
                    }
                }

                // display layout properties
                displayProperties(propertiesContainer, layoutProperties, isEditable);
            }
        }
    }

    /**
     * This method will clear all the generated UI for the current task and it will reset the map
     * containing changes for the current task.
     *
     * @param propertiesContainer
     */
    public void clearTaskLayout(LinearLayout propertiesContainer) {
        // clear UI
        propertiesContainer.removeAllViews();
        propertiesContainer.refreshDrawableState();

        // reset state
        mUpdatedValues.clear();
    }

    /**
     * Get a map with the updated values of the task properties.
     *
     * @return
     */
    public Map<String, String> getUpdatedProperties() {
        return mUpdatedValues;
    }

    private void displayProperties(LinearLayout taskContainer, List<ICMProperty> propertyList, boolean isEditable) {

        for (final ICMProperty p : propertyList) {
            if (p != null) {
                View v = mInflater.inflate(R.layout.layout_task_row, null, false);

                final TextView propertyName = (TextView) v.findViewById(R.id.property_name);
                final EditText propertyValue = (EditText) v.findViewById(R.id.property_value);
                final CheckBox propertyCheckbox = (CheckBox) v.findViewById(R.id.property_checkbox);
                final Button propertyActionButton = (Button) v.findViewById(R.id.property_picker);

                propertyName.setText(p.getDisplayName());

                // property value edit text will be disabled by default
                boolean noValue = p.getValue() == null;
                propertyValue.setText(noValue ? "" : String.valueOf(p.getValue()));
                propertyValue.setEnabled(false);

                // make some adjustments based on property type
                if (p.getType() == ICMProperty.IBMPropertyTypeBoolean) {
                    propertyValue.setVisibility(View.GONE);

                    // update the value of the checkbox
                    propertyCheckbox.setChecked(noValue ? false : Boolean.valueOf(p.getValue().toString()));
                    propertyCheckbox.setVisibility(View.VISIBLE);

                    // check box is disabled by default
                    propertyCheckbox.setEnabled(false);
                } else if (p.getType() == ICMProperty.IBMPropertyTypeTimestamp) {
                    //parse time zone
                    Date parseTimeZone = null;
                    if (!noValue) {
                        parseTimeZone = Utils.parseTimeZone(p.getValue().toString());
                    }

                    // format final value
                    if (parseTimeZone != null) {
                        propertyValue.setText(Utils.formatDate(parseTimeZone));
                    }
                }

                // if editing is allowed and property is not read only - enable editing
                if (isEditable && !p.isReadonly()) {
                    enableEditing(p, propertyValue, propertyCheckbox, propertyActionButton);
                }

                // finally, add to main container
                taskContainer.addView(v);
            }
        }
    }

    private void enableEditing(ICMProperty property, EditText propertyValue,
                               CheckBox propertyCheckbox, Button propertyActionButton) {

        switch (property.getType()) {
            case ICMProperty.IBMPropertyTypeInteger:
                propertyValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                propertyValue.setEnabled(true);
                enableTextValueEditor(property, propertyValue);
                break;
            case ICMProperty.IBMPropertyTypeDouble:
                propertyValue.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL);
                propertyValue.setEnabled(true);
                enableTextValueEditor(property, propertyValue);
                break;
            case ICMProperty.IBMPropertyTypeBoolean:
                propertyValue.setVisibility(View.GONE);
                propertyCheckbox.setVisibility(View.VISIBLE);
                enableBooleanEditor(property, propertyCheckbox);
                break;
            case ICMProperty.IBMPropertyTypeTimestamp:
                propertyActionButton.setVisibility(View.VISIBLE);
                enableDateEditor(property, propertyValue, propertyActionButton);
                break;
            default:
                propertyValue.setEnabled(true);
                enableTextValueEditor(property, propertyValue);
                break;
        }

    }

    private void enableDateEditor(final ICMProperty property, final EditText propertyValue,
                                  final Button propertyActionButton) {
        propertyActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a new instance of DatePickerDialog and return it
                showDateTimePickerDialog(property, propertyValue);
            }
        });
    }

    private void showDateTimePickerDialog(final ICMProperty property, final EditText propertyValue) {
        final int year = mCalendar.get(Calendar.YEAR);
        final int month = mCalendar.get(Calendar.MONTH);
        final int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog dialog = new android.app.DatePickerDialog(mContext,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mCalendar.set(year, monthOfYear, dayOfMonth);

                        TimePickerDialog timeDialog = new TimePickerDialog(mContext,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        mCalendar.set(Calendar.MINUTE, minute);

                                        propertyValue.setText(Utils.formatDate(mCalendar.getTime()));

                                        // also update property map
                                        updateProperty(property,
                                                Utils.formatDateWithTimeZone(mCalendar.getTime()));

                                    }
                                }, 12, 00, DateFormat.is24HourFormat(mContext));

                        timeDialog.show();
                    }
                }, year, month, day);

        dialog.show();
    }

    private void enableBooleanEditor(final ICMProperty property, CheckBox propertyCheckbox) {
        propertyCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                updateProperty(property, String.valueOf(isChecked));
            }
        });
    }

    private void enableTextValueEditor(final ICMProperty property, EditText propertyValue) {
        propertyValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateProperty(property, s.toString());
            }
        });
    }

    private void updateProperty(ICMProperty property, String value) {

        // update property map
        mUpdatedValues.put(property.getSymbolicName(), value);

        // notify listeners
        if (mPropertyChangeListener != null) {
            mPropertyChangeListener.onPropertyChanged(property, value);
        }

    }

}
