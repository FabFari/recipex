/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2016-04-08 17:16:44 UTC)
 * on 2016-05-03 at 20:44:14 UTC 
 * Modify at your own risk.
 */

package com.appspot.recipex_1281.recipexServerApi.model;

/**
 * Model definition for MainUserInfoMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the recipexServerApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MainUserInfoMessage extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String address;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String available;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String bio;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String birth;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("business_num")
  private java.lang.String businessNum;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<MainUserMainInfoMessage> caregivers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String city;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String email;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String field;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String name;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<MainUserMainInfoMessage> patients;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("pc_physician")
  private MainUserMainInfoMessage pcPhysician;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("personal_num")
  private java.lang.String personalNum;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String pic;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String place;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<MainUserMainInfoMessage> relatives;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private MainDefaultResponseMessage response;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String sex;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String surname;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("visiting_nurse")
  private MainUserMainInfoMessage visitingNurse;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("years_exp") @com.google.api.client.json.JsonString
  private java.lang.Long yearsExp;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getAddress() {
    return address;
  }

  /**
   * @param address address or {@code null} for none
   */
  public MainUserInfoMessage setAddress(java.lang.String address) {
    this.address = address;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getAvailable() {
    return available;
  }

  /**
   * @param available available or {@code null} for none
   */
  public MainUserInfoMessage setAvailable(java.lang.String available) {
    this.available = available;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getBio() {
    return bio;
  }

  /**
   * @param bio bio or {@code null} for none
   */
  public MainUserInfoMessage setBio(java.lang.String bio) {
    this.bio = bio;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getBirth() {
    return birth;
  }

  /**
   * @param birth birth or {@code null} for none
   */
  public MainUserInfoMessage setBirth(java.lang.String birth) {
    this.birth = birth;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getBusinessNum() {
    return businessNum;
  }

  /**
   * @param businessNum businessNum or {@code null} for none
   */
  public MainUserInfoMessage setBusinessNum(java.lang.String businessNum) {
    this.businessNum = businessNum;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<MainUserMainInfoMessage> getCaregivers() {
    return caregivers;
  }

  /**
   * @param caregivers caregivers or {@code null} for none
   */
  public MainUserInfoMessage setCaregivers(java.util.List<MainUserMainInfoMessage> caregivers) {
    this.caregivers = caregivers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getCity() {
    return city;
  }

  /**
   * @param city city or {@code null} for none
   */
  public MainUserInfoMessage setCity(java.lang.String city) {
    this.city = city;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getEmail() {
    return email;
  }

  /**
   * @param email email or {@code null} for none
   */
  public MainUserInfoMessage setEmail(java.lang.String email) {
    this.email = email;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getField() {
    return field;
  }

  /**
   * @param field field or {@code null} for none
   */
  public MainUserInfoMessage setField(java.lang.String field) {
    this.field = field;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public MainUserInfoMessage setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getName() {
    return name;
  }

  /**
   * @param name name or {@code null} for none
   */
  public MainUserInfoMessage setName(java.lang.String name) {
    this.name = name;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<MainUserMainInfoMessage> getPatients() {
    return patients;
  }

  /**
   * @param patients patients or {@code null} for none
   */
  public MainUserInfoMessage setPatients(java.util.List<MainUserMainInfoMessage> patients) {
    this.patients = patients;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public MainUserMainInfoMessage getPcPhysician() {
    return pcPhysician;
  }

  /**
   * @param pcPhysician pcPhysician or {@code null} for none
   */
  public MainUserInfoMessage setPcPhysician(MainUserMainInfoMessage pcPhysician) {
    this.pcPhysician = pcPhysician;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getPersonalNum() {
    return personalNum;
  }

  /**
   * @param personalNum personalNum or {@code null} for none
   */
  public MainUserInfoMessage setPersonalNum(java.lang.String personalNum) {
    this.personalNum = personalNum;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getPic() {
    return pic;
  }

  /**
   * @param pic pic or {@code null} for none
   */
  public MainUserInfoMessage setPic(java.lang.String pic) {
    this.pic = pic;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getPlace() {
    return place;
  }

  /**
   * @param place place or {@code null} for none
   */
  public MainUserInfoMessage setPlace(java.lang.String place) {
    this.place = place;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<MainUserMainInfoMessage> getRelatives() {
    return relatives;
  }

  /**
   * @param relatives relatives or {@code null} for none
   */
  public MainUserInfoMessage setRelatives(java.util.List<MainUserMainInfoMessage> relatives) {
    this.relatives = relatives;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public MainDefaultResponseMessage getResponse() {
    return response;
  }

  /**
   * @param response response or {@code null} for none
   */
  public MainUserInfoMessage setResponse(MainDefaultResponseMessage response) {
    this.response = response;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getSex() {
    return sex;
  }

  /**
   * @param sex sex or {@code null} for none
   */
  public MainUserInfoMessage setSex(java.lang.String sex) {
    this.sex = sex;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getSurname() {
    return surname;
  }

  /**
   * @param surname surname or {@code null} for none
   */
  public MainUserInfoMessage setSurname(java.lang.String surname) {
    this.surname = surname;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public MainUserMainInfoMessage getVisitingNurse() {
    return visitingNurse;
  }

  /**
   * @param visitingNurse visitingNurse or {@code null} for none
   */
  public MainUserInfoMessage setVisitingNurse(MainUserMainInfoMessage visitingNurse) {
    this.visitingNurse = visitingNurse;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getYearsExp() {
    return yearsExp;
  }

  /**
   * @param yearsExp yearsExp or {@code null} for none
   */
  public MainUserInfoMessage setYearsExp(java.lang.Long yearsExp) {
    this.yearsExp = yearsExp;
    return this;
  }

  @Override
  public MainUserInfoMessage set(String fieldName, Object value) {
    return (MainUserInfoMessage) super.set(fieldName, value);
  }

  @Override
  public MainUserInfoMessage clone() {
    return (MainUserInfoMessage) super.clone();
  }

}
