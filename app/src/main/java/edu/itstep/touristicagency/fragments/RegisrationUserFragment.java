package edu.itstep.touristicagency.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.Gson;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import edu.itstep.touristicagency.MainActivity;
import edu.itstep.touristicagency.R;
import edu.itstep.touristicagency.model.ConnectionSettings;
import edu.itstep.userdata.UserInfo;


public class RegisrationUserFragment extends Fragment
{

    private EditText etEmail, etPass, etConfPass, etName, etLastName, etCellNumber;
    private Button btnOk;
    private String[] newUserInfo;
    private String index;
    private ConnectionSettings connectionSettings;
    private Socket socket;
    private PrintWriter pw;
    private Scanner sc;
    private Gson gson;
    private String[] newUserInfoToServer;
    private String gsonStringToServer;
    private String gsonStingFromServerUserInfo;
    private String gsonStingFromServerCountries;
    private String gsonStingFromServerDurations;
    private UserInfo userInfo;

    public RegisrationUserFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_regisration_user, container, false);

        etEmail = view.findViewById(R.id.etEmail);
        etPass = view.findViewById(R.id.etPassword);
        etConfPass = view.findViewById(R.id.etConfPassword);
        etName = view.findViewById(R.id.etName);
        etLastName = view.findViewById(R.id.etLastname);
        etCellNumber = view.findViewById(R.id.etCellnumber);

        btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ConnectionAsyncTask connectionAsyncTask = new ConnectionAsyncTask();
                connectionAsyncTask.execute();

                if(etPass.getText().toString().equals(etConfPass.getText().toString()))
                {
                    createRegistrationRequest();
                    Toast.makeText(getContext(),getString(R.string.connecting),Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getContext(),getString(R.string.confirm_pass_erorr),Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private void createRegistrationRequest()
    {
        index = "3";
        newUserInfo = new String[]{index, etEmail.getText().toString(), etPass.getText().toString(),
                etName.getText().toString(), etLastName.getText().toString(),
                etCellNumber.getText().toString()};

        RegisrationNewUserAsyncTask regisrationNewUserAsyncTask = new RegisrationNewUserAsyncTask();
        regisrationNewUserAsyncTask.execute(newUserInfo);
    }


    private void showToast()
    {
        Toast.makeText(getContext(),getString(R.string.email_error),Toast.LENGTH_LONG).show();
    }

    private void changeFragment(Bundle bundle)
    {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        UserFragment userFragment = new UserFragment();
        userFragment.setArguments(bundle);
        ft.replace(R.id.fragments, userFragment);
        ft.commit();
    }

    public class RegisrationNewUserAsyncTask extends AsyncTask<String[],Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            gson = new Gson();
        }

        @Override
        protected Void doInBackground(String[]... params)
        {

            try
            {
                pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
                sc = new Scanner(socket.getInputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            send(params[0]);
            receive();

            return null;
        }

        private void send(String[] newUserInfoToServer)
        {
            gsonStringToServer = gson.toJson(newUserInfoToServer);
            pw.println(gsonStringToServer);
            pw.flush();
            Log.d("tag", "REGISTRATION send: " + gsonStringToServer);
        }

        private void receive()
        {
                gsonStingFromServerUserInfo = sc.nextLine();
                Log.d("tag", "REGISTRATION receive: " + gsonStingFromServerUserInfo);
                userInfo = gson.fromJson(gsonStingFromServerUserInfo, UserInfo.class);

                if(!userInfo.getId().equals("0"))
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(MainActivity.KEY_USER_INFO, gsonStingFromServerUserInfo);
                    changeFragment(bundle);
                }
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            if(userInfo.getId().equals("0"))
            {
                showToast();
            }
        }
    }

    private class ConnectionAsyncTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            try
            {
                connectionSettings = new ConnectionSettings();
                socket = new Socket(connectionSettings.getIp(), connectionSettings.getPort());
                MainActivity ma = (MainActivity) getActivity();
                ma.setSocket(socket);
                Log.d("tag", "Socket received");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }
}
