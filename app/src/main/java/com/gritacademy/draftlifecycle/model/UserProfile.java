package com.gritacademy.draftlifecycle.model;

import java.util.Calendar;

import com.google.firebase.database.Exclude;

public class UserProfile {

    private String name;
    private String email;
    private String birthdate;
    private Gender gender;
    private Integer height;
    private Integer weight;
    private boolean newsletter;
    // finns inget riktigt utskick, endast för att få in checkbox som input type

    public UserProfile() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public Integer getHeight() {
        return height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
    public Integer getWeight() {
        return weight;
    }
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    public boolean isNewsletter() {
        return newsletter;
    }
    public void setNewsletter(boolean newsletter) {
        this.newsletter = newsletter;
    }

    /** @Exclude = är inte en del av själva profile-objektet,
      * används för att räkna ut andra värden */

    /** ålder visas i år utifrån birthdate ("yyyy-mm-dd").
     * blir -1 om saknas/trasig (vilket visas som "-") */
    @Exclude
    public int getAge() {
        if (birthdate == null) return -1;
        String[] parts = birthdate.split("-");
        if (parts.length != 3) return -1;
        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - year;
            int monthToday = today.get(Calendar.MONTH) + 1; // Calendar.MONTH är 0-baserad
            int dayToday = today.get(Calendar.DAY_OF_MONTH);
            if (monthToday < month || (monthToday == month && dayToday < day)) {
                age--; // födelsedagen har inte varit i år än
            }
            return age;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** BMI = kg / m². NaN om height/weight saknas eller height är 0 */
    @Exclude
    public double getBmi() {
        if (height == null || weight == null || height == 0) return Double.NaN;
        double meters = height / 100.0;
        return weight / (meters * meters);
    }
}
