package edu.itstep.touristicagency.fragments;


import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.model.TableColumnPxWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import edu.itstep.tourdata.TourData;
import edu.itstep.touristicagency.MainActivity;
import edu.itstep.touristicagency.R;
import edu.itstep.userdata.UserInfo;


public class UserFragment extends Fragment implements View.OnClickListener
{
    private TextView tvUserInfo, tvDate;
    private Spinner spCountriea, spDuration;
    private Button btnSearch, btnClearDate;
    private TableView<String[]> tableView;
    private Gson gson;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapter;

    private UserInfo userInfo;
    private TableViewContentMaker tableViewContentMaker;

    private String gsonStringFromServerUserInfo;
    private String gsonStringFromServerCountriesList;
    private String gsonStringFromServerDurationsList;
    private String[] tableViewHeaders; //columns head's names
    private String day, month, meYear;
    private Socket socket;
    private Scanner scanner;
    private PrintWriter pw;
    private Type type;
    private ArrayList<TourData> tourDataArrayList;
    private String index, date, country, duration;
    private String[] requestToServer;
    private String gsonStringRequestToServer, gsonStringRequestAnswerFromServer;
    private String[][] tourDataArrayListForTableView;
    private String str;
    private int indexRowTableView;

    private Bundle bundle;


    private EnterFragment enterFragment;
    private ProfileFragment profileFragment;



    private String[] userEmailAndPass;
    private String gsonStringToSend;
    private Scanner sc;

    public UserFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        gson = new Gson();

        tvUserInfo = view.findViewById(R.id.tvUserInfo);
        tvDate = view.findViewById(R.id.tvDate);
        spCountriea = view.findViewById(R.id.spCountries);
        spDuration = view.findViewById(R.id.spDuration);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnClearDate = view.findViewById(R.id.btnClearDate);
        tableView = view.findViewById(R.id.tableView);

        setHasOptionsMenu(true);

        btnClearDate.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        tvDate.setOnClickListener(this);

        actions();

        return view;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnClearDate:
                tvDate.setText(getString(R.string.choose_date));
                break;
            case R.id.btnSearch:
                TableViewContentMaker tableViewContentMaker = new TableViewContentMaker();
                tableViewContentMaker.execute("2", tvDate.getText().toString(),
                                                spCountriea.getSelectedItem().toString(),
                                                spDuration.getSelectedItem().toString());
                break;
            case R.id.tvDate:
                calendar();
                break;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemProfile:
                bundleSend();
                FragmentTransaction ft1 = getActivity().getSupportFragmentManager().beginTransaction();
                profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);
                ft1.replace(R.id.fragments, profileFragment);
                ft1.addToBackStack(null);
                ft1.commit();
                break;
            case R.id.itemMyTours:
                FragmentTransaction ft2 = getActivity().getSupportFragmentManager().beginTransaction();
                ToursFragment toursFragment = new ToursFragment();
                //toursFragment.setArguments(bundle);
                ft2.replace(R.id.fragments, toursFragment);
                ft2.addToBackStack(null);
                ft2.commit();
                break;
            case R.id.itemLogOut:
                FragmentTransaction ft3 = getActivity().getSupportFragmentManager().beginTransaction();
                enterFragment = new EnterFragment();
                ft3.replace(R.id.fragments, enterFragment);
                ft3.commit();
                break;
        }
        return true;
    }

    private void calendar()
    {
        DatePickerDialog dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener()
        {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth)
            {
                day = String.valueOf(dayOfMonth);
                month = String.valueOf((monthOfYear + 1));
                meYear = String.valueOf(year);
                if (monthOfYear+1 < 10)
                {
                    month = 0 + month;
                }
                if (dayOfMonth < 10)
                {
                    day = 0 + day;
                }
                tvDate.setText(day + "-" + month + "-" + meYear);
            }
        }, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void actions()
    {
        prepareActions();
        createTableView();
        //bundleSend();
    }

    private void prepareActions()
    {
        Bundle bundleReceive = getArguments();
        gsonStringFromServerUserInfo = bundleReceive.getString(MainActivity.KEY_USER_INFO);
        userInfo = gson.fromJson(gsonStringFromServerUserInfo,UserInfo.class);

        MainActivity ma = (MainActivity) getActivity();
        socket = ma.getSocket();

        UserInfoAsyncTask userInfoAsyncTask = new UserInfoAsyncTask();
        userInfoAsyncTask.execute(userInfo.geteMail(),userInfo.getPassword());
    }

    private void createTableView()
    {
        tableViewHeaders = new String[]{"Date", "Duration","Country"};
        tableView.setHeaderBackgroundColor(Color.parseColor("#2ecc71"));
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(getContext(),tableViewHeaders));
        tableView.setColumnCount(3);

        TableColumnPxWidthModel columnModel = new TableColumnPxWidthModel(3, 200);
        columnModel.setColumnWidth(0, 230);
        columnModel.setColumnWidth(1, 200);
        columnModel.setColumnWidth(2, 300);

        tableView.setColumnModel(columnModel);

        tableView.addDataLongClickListener(new TableDataLongClickListener<String[]>()
        {
            @Override
            public boolean onDataLongClicked(int rowIndex, String[] clickedData)
            {
                indexRowTableView = rowIndex;
                return false;
            }
        });

        tableView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener()
        {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
            {
                menu.setHeaderTitle("Full information:");
                menu.add(0,0,0,"Date: " + tourDataArrayList.get(indexRowTableView).getDate());
                menu.add(0,0,0,"Cost: " + tourDataArrayList.get(indexRowTableView).getCoast());
                menu.add(0,0,0,"Country: " + tourDataArrayList.get(indexRowTableView).getCountry());
                menu.add(0,0,0,"Duration: " + tourDataArrayList.get(indexRowTableView).getDuration());
                menu.add(0,0,0, "Airline: " + tourDataArrayList.get(indexRowTableView).getAirline());

                getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
            }
        });
    }

    private void bundleSend()
    {
        bundle = new Bundle();
        bundle.putString(MainActivity.KEY_USER_INFO, gsonStringFromServerUserInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemBuy:
                SendBougthTourToDataBaseAsyncTask sendBougthTourToDataBaseAsyncTask = new SendBougthTourToDataBaseAsyncTask();
                sendBougthTourToDataBaseAsyncTask.execute("4", userInfo.getId(),
                        tourDataArrayList.get(indexRowTableView).getId(),
                        tourDataArrayList.get(indexRowTableView).getDate(),
                        tourDataArrayList.get(indexRowTableView).getCoast(),
                        tourDataArrayList.get(indexRowTableView).getDuration(),
                        tourDataArrayList.get(indexRowTableView).getCountry(),
                        tourDataArrayList.get(indexRowTableView).getAirline());
                break;

        }
        return super.onContextItemSelected(item);
    }

    private void setAllViewObjects()
    {
        userInfo = gson.fromJson(gsonStringFromServerUserInfo, UserInfo.class);
        Log.d("tag", "USER setAllViewObjects: " + gsonStringFromServerUserInfo);
        tvUserInfo.setText("Hello, " + userInfo.getName() + "!");

        arrayList = gson.fromJson(gsonStringFromServerCountriesList, ArrayList.class);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, arrayList);
        spCountriea.setAdapter(adapter);

        arrayList = gson.fromJson(gsonStringFromServerDurationsList, ArrayList.class);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, arrayList);
        spDuration.setAdapter(adapter);
    }

    private void showToast()
    {
        Toast.makeText(getContext(),getString(R.string.bougth_meassage), Toast.LENGTH_LONG).show();
    }


    private class TableViewContentMaker extends AsyncTask<String, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            str = getString(R.string.click_here);
            gson = new Gson();
            type = new TypeToken<ArrayList<TourData>>(){}.getType();
        }

        @Override
        protected Void doInBackground(String... params)
        {
            requestToServer = new String[]{params[0], params[1], params[2], params[3]};
            try
            {
                pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
                scanner = new Scanner(socket.getInputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            send(requestToServer);
            receive();
            updateTableView(tourDataArrayList);

            return null;
        }

        private void send(String[] requestToServer)
        {
            for(int i=0;i<requestToServer.length;i++)
            {
                if(requestToServer[i].equals(str) || requestToServer[i].equals(" "))
                {
                    requestToServer[i] = "0";
                }

            }
            Log.d("tag", "requestToServer " + requestToServer[0] + " " + requestToServer[1] + " " + requestToServer[2] + " " + requestToServer[3]);
            gsonStringRequestToServer = gson.toJson(requestToServer);
            pw.println(gsonStringRequestToServer);
            pw.flush();
        }

        private void receive()
        {
            Log.d("tag", "USER receive TableViewContentMaker: " + gsonStringRequestAnswerFromServer);

            gsonStringRequestAnswerFromServer = scanner.nextLine();
            Log.d("tag", "USER  receive TableViewContentMaker: " + gsonStringRequestAnswerFromServer);
            tourDataArrayList = gson.fromJson(gsonStringRequestAnswerFromServer,type);

        }

        private void updateTableView(ArrayList<TourData> tourDataArrayList)
        {
            tourDataArrayListForTableView = new String[tourDataArrayList.size()][3];
            for(int i=0;i<tourDataArrayList.size();i++)
            {
                tourDataArrayListForTableView[i][0] = tourDataArrayList.get(i).getDate();
                tourDataArrayListForTableView[i][1] = tourDataArrayList.get(i).getDuration();
                tourDataArrayListForTableView[i][2] = tourDataArrayList.get(i).getCountry();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            tableView.setDataAdapter(new SimpleTableDataAdapter(getContext(), tourDataArrayListForTableView));
        }
    }

    private class SendBougthTourToDataBaseAsyncTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            gson = new Gson();
            type = new TypeToken<ArrayList<TourData>>(){}.getType();
        }

        @Override
        protected Void doInBackground(String... params)
        {
            requestToServer = new String[]{params[0], params[1], params[2], params[3],
                                           params[4], params[5], params[6], params[7]};
            try
            {
                pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
                scanner = new Scanner(socket.getInputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            send(requestToServer);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            showToast();
        }

        private void send(String[] requestToServer)
        {
            gsonStringRequestToServer = gson.toJson(requestToServer);
            pw.println(gsonStringRequestToServer);
            pw.flush();
            Log.d("tag", "USER SendBougthTourToDataBaseAsyncTask send : " + gsonStringRequestToServer);
        }

    }
    private class UserInfoAsyncTask extends AsyncTask<String, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            gson = new Gson();
        }

        @Override
        protected Void doInBackground(String... params)
        {
            try
            {
                userEmailAndPass = new String[]{"1", params[0], params[1]};
                pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
                sc = new Scanner(socket.getInputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            send(userEmailAndPass);
            receiveCountriesList();
            receiveDurationsList();
            receive();
            return null;
        }

        private void send(String[] userEmailAndPass)
        {
            gsonStringToSend = gson.toJson(userEmailAndPass);
            Log.d("tag", "USER send: " + gsonStringToSend);
            pw.println(gsonStringToSend);
            pw.flush();
        }

        private void receiveCountriesList()
        {
            gsonStringFromServerCountriesList = sc.nextLine();
            Log.d("tag", "USER receiveCountriesList: " + gsonStringFromServerCountriesList);
        }

        private void receiveDurationsList()
        {
            gsonStringFromServerDurationsList = sc.nextLine();
            Log.d("tag", "USER receiveDurationsList: " + gsonStringFromServerDurationsList);
        }

        private void receive()
        {
            gsonStringFromServerUserInfo = sc.nextLine();
            Log.d("tag", "USER receive: " + gsonStringFromServerUserInfo);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            setAllViewObjects();
        }
    }
}
