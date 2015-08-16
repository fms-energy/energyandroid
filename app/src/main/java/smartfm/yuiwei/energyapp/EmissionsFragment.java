package smartfm.yuiwei.energyapp;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EmissionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EmissionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmissionsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final int SUBCOLUMNS_DATA = 1;
/*
    private ColumnChartView chart;
    private ColumnChartData data;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLabels = false;
    private boolean hasLabelForSelected = false;
    private int dataType = SUBCOLUMNS_DATA;


    private LineChartView chart;
    private LineChartData data;*/
    //Deep copy of data.


    private LineChartView chartTop;
    private ColumnChartView chartBottom;

    private LineChartData lineData;
    private ColumnChartData columnData;

    private ArrayList<ArrayList<PointValue>> emissionsOverMonth;
    private ArrayList<ArrayList<PointValue>> travEmissionsOverMonth;
    private ArrayList<ArrayList<PointValue>> stopEmissionsOverMonth;
    private ArrayList<Float> monthlyEmissions;
    private ArrayList<ArrayList<PointValue>> energyOverMonth;
    private ArrayList<Float> monthlyEnergy;
    ArrayList<EmissionDataPoint> emissionsData;





    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EmissionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EmissionsFragment newInstance(String param1, String param2) {
        EmissionsFragment fragment = new EmissionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public EmissionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            emissionsData = getArguments().getParcelableArrayList("emissionsData");
            emissionsOverMonth = new ArrayList<>(32);
            travEmissionsOverMonth = new ArrayList<>(32);
            stopEmissionsOverMonth = new ArrayList<>(32);

            energyOverMonth = new ArrayList<>(32);
            monthlyEmissions = new ArrayList<>(32);
            monthlyEnergy = new ArrayList<>(32);
            float zero = 0;

            for (int i = 0; i < 32; i++) {
                emissionsOverMonth.add(blankValueList(1));
                travEmissionsOverMonth.add(blankValueList(1));
                stopEmissionsOverMonth.add(blankValueList(1));
                energyOverMonth.add(blankValueList(1));
                monthlyEmissions.add(zero);
                monthlyEnergy.add(zero);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_emissions, container, false);

        chartTop = (LineChartView) rootView.findViewById(R.id.chart_top);
        chartBottom = (ColumnChartView) rootView.findViewById(R.id.chart_bottom);


        if (getArguments() != null) {
            ArrayList<EmissionDataPoint> data = new ArrayList<>();

            setEmissionsData(emissionsData);
            generateInitialLineData();
            generateColumnData();
        } else {
            Log.d("HELP", "help");
        }

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onEmissionsFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onEmissionsFragmentInteraction(Uri uri);
    }

    private void setEmissionsData(ArrayList<EmissionDataPoint> edps) {

        int ctr=0; float minuteCount=0; float dailyEmissions=0; float dailyEnergy=0;
        ArrayList<PointValue> emissionsOverDay = new ArrayList<>();
        ArrayList<PointValue> travEmissionsOverDay = new ArrayList<>();
        ArrayList<PointValue> stopEmissionsOverDay = new ArrayList<>();
        ArrayList<PointValue> energyOverDay = new ArrayList<>();

        while(ctr!=edps.size()) {
            EmissionDataPoint edp = edps.get(ctr);

            minuteCount+=edp.travel.duration;
            float travelEm = edp.travel.emissions;
            Log.d("VALUES/travelEM", Float.toString(travelEm));

            float travelEn = edp.travel.energy;
            Log.d("VALUES/travelEM", Float.toString(travelEn));

            dailyEmissions+=travelEm;
            dailyEnergy+=travelEn;
            travEmissionsOverDay.add(new PointValue(minuteCount,dailyEmissions));
            emissionsOverDay.add(new PointValue(minuteCount,dailyEmissions));
            energyOverDay.add(new PointValue(minuteCount,dailyEnergy));

            minuteCount+=edp.stop.duration;
            float stopEm = edp.stop.emissions;
            float stopEn = edp.stop.energy;
            dailyEmissions+=stopEm;
            dailyEnergy+=stopEn;
            stopEmissionsOverDay.add(new PointValue(minuteCount,dailyEmissions));
            emissionsOverDay.add(new PointValue(minuteCount,dailyEmissions));
            energyOverDay.add(new PointValue(minuteCount,dailyEnergy));
            if(edp.dayNew==1) {
                Log.d("VALUES/overday", emissionsOverDay.toString());
                Log.d("VALUES/overday", Double.toString(dailyEmissions));

                emissionsOverMonth.set(edp.dateDay, emissionsOverDay);
                travEmissionsOverMonth.set(edp.dateDay, travEmissionsOverDay);
                stopEmissionsOverMonth.set(edp.dateDay, stopEmissionsOverDay);
                monthlyEmissions.set(edp.dateDay,dailyEmissions);
                emissionsOverDay = new ArrayList<PointValue>();
                travEmissionsOverDay = new ArrayList<PointValue>();
                stopEmissionsOverDay = new ArrayList<PointValue>();

                energyOverMonth.set(edp.dateDay, energyOverDay);
                monthlyEnergy.set(edp.dateDay,dailyEnergy);
                energyOverDay = new ArrayList<PointValue>();

                minuteCount=edp.dayStart; dailyEmissions=0; dailyEnergy=0;
            }
            ctr+=1;
        }
        Log.d("VALUES", monthlyEmissions.toString());
        Log.d("VALUES", monthlyEnergy.toString());

    }


        private void generateColumnData() {

        int numSubcolumns = 1;
        int numColumns = 31;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        List<SubcolumnValue> values2;
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<SubcolumnValue>();
            values2 = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; j++) {
               // values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
                if (j==0) {
                    values.add(new SubcolumnValue(monthlyEmissions.get(i), ChartUtils.COLOR_BLUE));
                } else {
                    values2.add(new SubcolumnValue(monthlyEnergy.get(i), ChartUtils.COLOR_VIOLET));
                }
            }

            axisValues.add(new AxisValue(i).setLabel(Integer.toString(i)));

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
            //columns.add(new Column(values2).setHasLabelsOnlyForSelected(true));
        }

        columnData = new ColumnChartData(columns);
        columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
        columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(2));
        chartBottom.setColumnChartData(columnData);

        // Set value touch listener that will trigger changes for chartTop.
        chartBottom.setOnValueTouchListener(new ValueTouchListener());

        // Set selection mode to keep selected month column highlighted.
        chartBottom.setValueSelectionEnabled(true);

        chartBottom.setZoomType(ZoomType.HORIZONTAL);
    }

    /**
     * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
     * will select value on column chart.
     */
    private void generateInitialLineData() {
        int numValues = 24;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<PointValue>();
        List<PointValue> values2 = new ArrayList<PointValue>();
        for (int i = 0; i < numValues; ++i) {
            values.add(new PointValue(i, 0));
            values2.add(new PointValue(i, 0));

            axisValues.add(new AxisValue(values.get(i).getX()).setLabel(Integer.toString(i)));
        }

        Line line = new Line(values);
        //Line line2 = new Line(values2);
        line.setColor(ChartUtils.COLOR_BLUE).setCubic(false);
        //line2.setColor(ChartUtils.COLOR_VIOLET).setCubic(false);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        //lines.add(line2);

        lineData = new LineChartData(lines);
        lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
        lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));
        chartTop.setLineChartData(lineData);

        // For build-up animation you have to disable viewport recalculation.
        chartTop.setViewportCalculationEnabled(true);

        // And set initial max viewport and current viewport- remember to set viewports after data.
        Viewport v = new Viewport(0, 110, 6, 0);
        chartTop.setMaximumViewport(v);
        chartTop.setCurrentViewport(v);

        chartTop.setZoomType(ZoomType.HORIZONTAL);
    }

    private void generateLineData(int color, float range, int colIndex, int subcolIndex) {
        // Cancel last animation if not finished.

        chartTop.cancelDataAnimation();

        ArrayList<PointValue> emissionsOverDay = emissionsOverMonth.get(colIndex);
        ArrayList<PointValue> energyOverDay = energyOverMonth.get(colIndex);
        ArrayList<PointValue> valuesUsed;
        ArrayList<PointValue> valuesUsed2;
        if(subcolIndex==0) {
            valuesUsed = emissionsOverDay;
        } else {
            valuesUsed = energyOverDay;
        }
        // Modify data targets
        Line line = new Line(valuesUsed);
        List axisValues = new ArrayList<AxisValue>();
        line.setColor(color).setCubic(false).setHasPoints(false);

        List<Line> lines = new ArrayList<Line>();
        for (int i=0;i<valuesUsed.size();i++) {
            axisValues.add(new AxisValue(valuesUsed.get(i).getX()).setLabel(Integer.toString(i)));
        }
        lines.add(line);

        lineData = new LineChartData(lines);
        lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
        lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

        chartTop.setLineChartData(lineData);
        // Start new data animation with 300ms duration;
       // chartTop.startDataAnimation(300);


    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            generateLineData(value.getColor(), 100, columnIndex, subcolumnIndex);
        }

        @Override
        public void onValueDeselected() {

            generateLineData(ChartUtils.COLOR_GREEN, 0, 0, 0);

        }
    }

    public ArrayList<PointValue> blankValueList(int size) {
        PointValue blank = new PointValue(0,0);
        ArrayList<PointValue> dummy = new ArrayList<>();
        for(int i=0; i<size; i++) {
            dummy.add(blank);
        }
        return dummy;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
