package nz.matthuisman.electrickiwi;

import android.content.Context;
import android.content.ContextWrapper;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ElectricKiwi extends ContextWrapper{
    private OkHttpClient client;
    private String email;
    private String password;

    private SessionPersistentCookieJar cookieJar;
    private final String login_url  = "https://my.electrickiwi.co.nz/login";
    private final String hour_url   = "https://my.electrickiwi.co.nz/account/update-hour-of-power";
    private final String user_agent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0";

    public ElectricKiwi(Context context) {
        super(context);

        cookieJar = new SessionPersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
    }

    public void logout() {
        cookieJar.clear();
    }

    public void setCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }

    public ArrayList<Hour> set_hour(String hour_id) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("KiwikPayment[free_hour_consumption]", hour_id)
                .build();

        // Create request for remote resource.
        Request request = new Request.Builder()
                .url(hour_url)
                .header("User-Agent", user_agent)
                .post(formBody)
                .build();

        Response response = do_request(request, true);
        String page = response.body().string();
        return parse_hours(page);
    }

    private ArrayList<Hour> parse_hours(String page) throws IOException {
        Document doc = Jsoup.parse(page);
        Elements select = doc.select("#KiwikPayment_free_hour_consumption");
        if(select == null || select.isEmpty()){
            throw new IOException("Unable to parse hours");
        }

        Elements options = select.get(0).children();

        ArrayList<Hour> hours = new ArrayList<>();
        for (Element option : options) {
            Hour hour = new Hour(option.attr("value"), option.text());
            if (option.hasAttr("selected")) {
                hour.setSelected(true);
            }
            hours.add(hour);
        }

        if (hours.isEmpty()) {
            throw new IOException("Unable to parse hours");
        }

        return hours;
    }

    public Hour get_hour() throws IOException {
        ArrayList<Hour> hours = get_hours();

        for (int i = 0; i < hours.size(); i++) {
            if (hours.get(i).isSelected()) {
                return hours.get(i);
            }
        }

        throw new IOException("Could not find current power hour");
    }

    public ArrayList<Hour> get_hours() throws IOException {
        Request request = new Request.Builder()
                .url(hour_url)
                .header("User-Agent", user_agent)
                .build();

        Response response = do_request(request, true);
        String page = response.body().string();
        return parse_hours(page);
    }

    protected Response do_request(Request request, boolean loginRequired) throws IOException {
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        if (loginRequired && response.request().url().toString().equals(login_url)) {
            response = this.login();
            if (response.request().url().equals(request.url())) {
                return response;
            }else {
                return do_request(request, false);
            }
        }

        return response;
    }

    protected Response login() throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("LoginForm[username]", email)
                .add("LoginForm[password]", password)
                .build();

        Request request = new Request.Builder()
                .url(login_url)
                .header("User-Agent", user_agent)
                .post(formBody)
                .build();

        Response response = do_request(request, false);
        if (response.request().url().toString().equals(login_url)) throw new IOException("Invalid credentials");
        return response;
    }
}