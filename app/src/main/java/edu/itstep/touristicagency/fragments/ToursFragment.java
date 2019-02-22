package edu.itstep.touristicagency.fragments;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
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

public class ToursFragment extends Fragment
{
    private TableView tableView;
    private String[] tableViewHeaders;
    private int indexRowTableView;
    private ArrayList<TourData> tourDataArrayList;
    private Gson gson;
    private PrintWriter pw;
    private Socket socket;
    private Scanner scanner;
    private String[] requestToServer;
    private String gsonStringRequestToServer;
    private String gsonStringRequestAnswerFromServer;
    private Type type;
    private String[][] tourDataArrayListForTableView;
    private String gsonStringUserInfo;
    private UserInfo userInfo;
    private Button btnBack;

    public ToursFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_tours, container, false);

        btnBack = view.findViewById(R.id.btnBack);
        tableView = view.findViewById(R.id.tableViewBoughtTours);

        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFragmentManager().popBackStack();
            }
        });

        actions();

        return view;
    }

    private void actions()
    {
        prepareActions();
        createTableView();
        getTableViewData();
    }

    private void prepareActions()
    {
        MainActivity ma = (MainActivity) getActivity();
        socket = ma.getSocket();
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

                getActivity().getMenuInflater().inflate(R.menu.context_menu_bougt_tours_fragment, menu);
            }
        });
    }

    private void getTableViewData()
    {
        TableViewContentMaker tableViewContentMaker = new TableViewContentMaker();
        tableViewContentMaker.execute();
    }


    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemDelete:
                DeleteBoughtTourAsyncTask deleteBoufhtTourAsyncTask = new DeleteBoughtTourAsyncTask();
                deleteBoufhtTourAsyncTask.execute(tourDataArrayList.get(indexRowTableView).getId());
                break;
        }
        return super.onContextItemSelected(item);
    }

    private class TableViewContentMaker extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            gson = new Gson();
            type = new TypeToken<ArrayList<TourData>>(){}.getType();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                requestToServer = new String[]{"5"};
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
            gsonStringRequestToServer = gson.toJson(requestToServer);
            pw.println(gsonStringRequestToServer);
            pw.flush();
        }

        private void receive()
        {
            gsonStringRequestAnswerFromServer = scanner.nextLine();
            Log.d("tag", "TOURS receive TableViewContentMaker: " + gsonStringRequestAnswerFromServer);
            if(!gsonStringRequestAnswerFromServer.equals("0"))
            {
                tourDataArrayList = gson.fromJson(gsonStringRequestAnswerFromServer,type);
            }

        }

        private void updateTableView(ArrayList<TourData> tourDataArrayList)
        {
            if(tourDataArrayList!=null)
            {
                tourDataArrayListForTableView = new String[tourDataArrayList.size()][3];
                for(int i=0;i<tourDataArrayList.size();i++)
                {
                    tourDataArrayListForTableView[i][0] = tourDataArrayList.get(i).getDate();
                    tourDataArrayListForTableView[i][1] = tourDataArrayList.get(i).getDuration();
                    tourDataArrayListForTableView[i][2] = tourDataArrayList.get(i).getCountry();
                }
            }

        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            if(tourDataArrayList!=null)
            {
                tableView.setDataAdapter(new SimpleTableDataAdapter(getContext(), tourDataArrayListForTableView));
            }
            else
            {
                createTableView();
            }
        }
    }

    private class DeleteBoughtTourAsyncTask extends AsyncTask<String, Void, Void>
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
                requestToServer = new String[]{"7", params[0]};
                pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
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
            getTableViewData();
        }

        private void send(String[] requestToServer)
        {
            gsonStringRequestToServer = gson.toJson(requestToServer);
            Log.d("tag", "TOURS send DeleteBoughtTourAsyncTask: " + gsonStringRequestToServer);
            pw.println(gsonStringRequestToServer);
            pw.flush();
        }
    }
}
