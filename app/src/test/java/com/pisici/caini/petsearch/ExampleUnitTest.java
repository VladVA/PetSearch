package com.pisici.caini.petsearch;

import android.net.ParseException;
import android.util.Log;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void scadere_date(){
        ProfileActivity activity=new ProfileActivity();
        SimpleDateFormat date=new SimpleDateFormat("dd/MM/YYYY");
        Date acum=Calendar.getInstance().getTime();
        Date atunci=new Date();
        try {
            atunci=date.parse("25/11/2018");
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        boolean expected=true;
        boolean result=activity.subtractDates(acum,atunci);
        assertEquals(expected,result);
    }

}