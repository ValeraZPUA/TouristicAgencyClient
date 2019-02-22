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

public class EnterFragment extends Fragment implements View.OnClickListener {

    private Button btnEnter, btnCreateNewUser;
    private EditText etEmail, etPassword;
    private UserFragment userFragment;
    private RegisrationUserFragment regisrationUserFragment;
    private ConnectionSettings connectionSettings;
    private Socket socket;
    private PrintWriter pw;
    private Scanner sc;
    private Gson gson;
    private UserInfo userInfo;
    private String[] userEmailAndPass;
    private String gsonStringToSend;
    private String gsonStingFromServerUserInfo;

    public EnterFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enter, container, false);

        socket = null;
        btnEnter = view.findViewById(R.id.btnEnter);
        btnCreateNewUser = view.findViewById(R.id.btnCreateNewUser);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        btnEnter.setOnClickListener(this);
        btnCreateNewUser.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnEnter:
                    ConnectionAsyncTask connectionAsyncTask = new ConnectionAsyncTask();
                    connectionAsyncTask.execute();

                if((!etEmail.equals("") && !etPassword.equals("")))
                {
                    EnterAsyncTask enterAsyncTask = new EnterAsyncTask();
                    enterAsyncTask.execute(etEmail.getText().toString(), etPassword.getText().toString());
                    Toast.makeText(getContext(),getString(R.string.connecting),Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getContext(),getString(R.string.error_entering),Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnCreateNewUser:
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                regisrationUserFragment = new RegisrationUserFragment();
                ft.replace(R.id.fragments, regisrationUserFragment);
                ft.commit();
                break;
        }
    }

    private void showToast()
    {
        String error = getString(R.string.error);
        Toast.makeText(getContext(),error,Toast.LENGTH_SHORT).show();
    }

    private void changeFragment(Bundle bundle)
    {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        userFragment = new UserFragment();
        userFragment.setArguments(bundle);
        ft.replace(R.id.fragments,  userFragment);
        ft.commit();
    }

    private class EnterAsyncTask extends AsyncTask<String, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //connectionSettings = new ConnectionSettings();
            gson = new Gson();
        }

        @Override
        protected Void doInBackground(String... params)
        {
            userEmailAndPass = new String[]{"0", params[0], params[1]};
            try
            {
                pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
                sc = new Scanner(socket.getInputStream());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            send(userEmailAndPass);
            receive();
            return null;
        }

        private void send(String[] userEmailAndPass)
        {
            gsonStringToSend = gson.toJson(userEmailAndPass);
            Log.d("tag", "ENTER  send: " + gsonStringToSend);
            pw.println(gsonStringToSend);
            pw.flush();
        }

        private void receive()
        {
            gsonStingFromServerUserInfo = sc.nextLine();
            Log.d("tag", "ENTER  receive: " + gsonStingFromServerUserInfo);
            userInfo = gson.fromJson(gsonStingFromServerUserInfo, UserInfo.class);

            if (!userInfo.geteMail().equals("0"))
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
            if (userInfo.geteMail().equals("0"))
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
                Log.d("tag", "ENTER Socket received");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }
}
