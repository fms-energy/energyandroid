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
import android.widget.ScrollView;
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
public class EmissionsFragment extends Fragment {

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
    private ArrayList<Float> monthlyEmissions;
    private ArrayList<ArrayList<PointValue>> energyOverMonth;
    private ArrayList<Float> monthlyEnergy;




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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_emissions, container, false);
        emissionsOverMonth = new ArrayList<>(31);
        energyOverMonth = new ArrayList<>(31);
        monthlyEmissions = new ArrayList<>(31);
        monthlyEnergy = new ArrayList<>(31);
        float zero = 0;

        for(int i=0;i<31;i++) {
            emissionsOverMonth.add(blankValueList(1));
            energyOverMonth.add(blankValueList(1));
            monthlyEmissions.add(zero);
            monthlyEnergy.add(zero);
        }
/*
        chart = (ColumnChartView) rootView.findViewById(R.id.chart);
        chart.setOnValueTouchListener(new ValueTouchListener());

        chart = (LineChartView) rootView.findViewById(R.id.chart);

        //setHasOptionsMenu(true);


        // Generate data for previewed chart and copy of that data for preview chart.

        if (getArguments() != null) {
            ArrayList<EmissionDataPoint> emissionsData = getArguments().getParcelableArrayList("emissionsData");

            setEmissionsData(emissionsData);
            Log.d("receiver", "OK");
        } else {
            //generateSubcolumnsData();
        }
        //chart.setColumnChartData(data);
        chart.setLineChartData(data);
*/


        chartTop = (LineChartView) rootView.findViewById(R.id.chart_top);

        // Generate and set data for line chart

        // *** BOTTOM COLUMN CHART ***

        chartBottom = (ColumnChartView) rootView.findViewById(R.id.chart_bottom);


        if (getArguments() != null) {
            ArrayList<EmissionDataPoint> emissionsData = getArguments().getParcelableArrayList("emissionsData");

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onEmissionsFragmentInteraction(Uri uri);
    }


/*
    private void generateSubcolumnsData() {
        int numSubcolumns = 2;
        int numColumns = 8;
        // Column can have many subcolumns, here I use 4 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; ++j) {
                values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
            }

            Column column = new Column(values);
            column.setHasLabels(hasLabels);
            column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.add(column);
        }

        data = new ColumnChartData(columns);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("Axis X");
                axisY.setName("Axis Y");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        chart.setColumnChartData(data);

    }
    //LINE AND COLUMN CHART
    private void setEmissionsData(ArrayList<EmissionDataPoint> edps) {
        int numSubcolumns = 4;
        int numColumns = edps.size();
        List<PointValue> energy = new ArrayList<>();
        List<PointValue> emissions = new ArrayList<>();

        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        float minuteCount = 0;
        for (int i = 0; i < numColumns; i++) {
            EmissionDataPoint edp = edps.get(i);
            //values = new ArrayList<SubcolumnValue>();

            AxisValue av;
            if (edp.dayNew == 1) { minuteCount = edp.dayStart; }

            int month = edp.dateMonth;
            int day = edp.dateDay;
            int dateVal = month*31+day; //simple ordering of dates
            Log.d("MC", Float.toString(minuteCount));

            av = new AxisValue(xVal);
            av.setLabel("" + day +"/" + month + ": " + minuteCount);
            axisValues.add(av);


            for (int j = 0; j < numSubcolumns; j++) {
                float val;
                int color;
                float xVal;



                if (j<2) {
                    Travel tr = edp.travel;
                    minuteCount+=tr.duration;
                    xVal = minuteCount/2880 + dateVal;
                    if (j%2 == 0) { val = tr.energy;  color = ChartUtils.COLOR_BLUE;
                        energy.add(new PointValue(xVal,val)); }
                    else { val = tr.emissions; color = ChartUtils.COLOR_GREEN;
                        emissions.add(new PointValue(xVal,val));}
                } else {
                    Stop st = edp.stop;
                    minuteCount+=st.duration;
                     xVal = minuteCount/2880 + dateVal;
                    if (j%2 == 0) {val = st.energy; color = ChartUtils.COLOR_BLUE;
                        energy.add(new PointValue(xVal,val));  }
                    else { val = st.emissions; color = ChartUtils.COLOR_GREEN;
                        emissions.add(new PointValue(xVal,val));}
                }

                //values.add(new SubcolumnValue(val,color));

                Log.d("chart", Float.toString(val));
                Log.d("VALUES_X", Float.toString(xVal));
            }

            Column column = new Column(values);
            column.setHasLabels(hasLabels);
            column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.add(column);



        }

        data = new ColumnChartData(columns);

        if (hasAxes) {
            Axis axisX = new Axis(axisValues);
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("Date");
                axisY.setName("Energy/Emission");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        chart.setColumnChartData(data);

        Line line = new Line(energy);
        Line line2 = new Line(emissions);
        line.setColor(ChartUtils.COLOR_GREEN);
        line2.setColor(ChartUtils.COLOR_BLUE);

       // line.setHasPoints(false);// too many values so don't draw points.
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        lines.add(line2);

        Line line2 = new Line(values2);
        line2.setColor(ChartUtils.COLOR_BLUE);
        line2.setHasPoints(false);
        lines.add(line2);

        data = new LineChartData(lines);
        data.setAxisXBottom(new Axis());
        data.setAxisYLeft(new Axis().setHasLines(true));


    }
    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }
*/

    private void setEmissionsData(ArrayList<EmissionDataPoint> edps) {
        int ctr=0; float minuteCount=0; float dailyEmissions=0; float dailyEnergy=0;
        ArrayList<PointValue> emissionsOverDay = new ArrayList<>();
        ArrayList<PointValue> energyOverDay = new ArrayList<>();

        while(ctr!=edps.size()) {
            EmissionDataPoint edp = edps.get(ctr);
            if(edp.dayNew==1) {
                emissionsOverMonth.set(edp.dateDay, emissionsOverDay);
                monthlyEmissions.set(edp.dateDay,dailyEmissions);
                emissionsOverDay = new ArrayList<PointValue>();

                energyOverMonth.set(edp.dateDay, energyOverDay);
                monthlyEnergy.set(edp.dateDay,dailyEnergy);
                energyOverDay = new ArrayList<PointValue>();

                minuteCount=edp.dayStart; dailyEmissions=0; dailyEnergy=0;
            }
            minuteCount+=edp.travel.duration;
            float travelEm = edp.travel.emissions;
            float travelEn = edp.travel.energy;
            dailyEmissions+=travelEm;
            dailyEnergy+=travelEn;
            emissionsOverDay.add(new PointValue(minuteCount,dailyEmissions));
            energyOverDay.add(new PointValue(minuteCount,dailyEnergy));

            minuteCount+=edp.stop.duration;
            float stopEm = edp.stop.emissions;
            float stopEn = edp.stop.energy;
            dailyEmissions+=stopEm;
            dailyEnergy+=stopEn;
            emissionsOverDay.add(new PointValue(minuteCount,dailyEmissions));
            energyOverDay.add(new PointValue(minuteCount,dailyEnergy));


            ctr+=1;
        }
        Log.d("VALUES", monthlyEmissions.toString());
    }


        private void generateColumnData() {

        int numSubcolumns = 1;
        int numColumns = 31;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; ++j) {
               // values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
                values.add(new SubcolumnValue(monthlyEmissions.get(i),ChartUtils.pickColor()));
                Log.d("VALUES", Float.toString(monthlyEmissions.get(i)));
            }

            axisValues.add(new AxisValue(i).setLabel(Integer.toString(i)));

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
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

        // chartBottom.setOnClickListener(new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // SelectedValue sv = chartBottom.getSelectedValue();
        // if (!sv.isSet()) {
        // generateInitialLineData();
        // }
        //
        // }
        // });

    }

    /**
     * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
     * will select value on column chart.
     */
    private void generateInitialLineData() {
        int numValues = 24;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<PointValue>();
        for (int i = 0; i < numValues; ++i) {
            values.add(new PointValue(i, 0));
            axisValues.add(new AxisValue(i).setLabel(Integer.toString(i)));
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_GREEN).setCubic(false);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

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

    private void generateLineData(int color, float range, int colIndex) {
        // Cancel last animation if not finished.

        chartTop.cancelDataAnimation();

        ArrayList<PointValue> emissionsOverDay = emissionsOverMonth.get(colIndex);
        // Modify data targets
        /*
        Line line = lineData.getLines().get(0);// For this example there is always only one line.
        line.setColor(color);

        line.setValues(emissionsOverDay);
        for (int i=0; i<emissionsOverDay.size();i++) {
            // Change target only for Y value.
            PointValue value = line.getValues().get(i);
            value.setTarget(emissionsOverDay.get(i).getX(),
                    emissionsOverDay.get(i).getY());
        }

        // Start new data animation with 300ms duration;
        chartTop.startDataAnimation(300);*/
        Line line = new Line(emissionsOverDay);
        List axisValues = new ArrayList<AxisValue>();
        line.setColor(color).setCubic(false).setHasPoints(false);

        List<Line> lines = new ArrayList<Line>();
        for (int i=0;i<emissionsOverDay.size();i++) {
            axisValues.add(new AxisValue(emissionsOverDay.get(i).getX()).setLabel(Integer.toString((int)emissionsOverDay.get(i).getX())));
        }
        lines.add(line);

        lineData = new LineChartData(lines);
        lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
        lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

        chartTop.setLineChartData(lineData);

    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            generateLineData(value.getColor(), 100, columnIndex);
        }

        @Override
        public void onValueDeselected() {

            generateLineData(ChartUtils.COLOR_GREEN, 0, 0);

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


}
