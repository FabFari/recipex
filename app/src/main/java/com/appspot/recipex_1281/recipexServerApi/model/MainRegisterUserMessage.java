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
 * on 2016-04-26 at 17:01:19 UTC 
 * Modify at your own risk.
 */

package com.appspot.recipex_1281.recipexServerApi.model;

/**
 * Model definition for MainRegisterUserMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the recipexServerApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MainRegisterUserMessage extends com.google.api.client.json.GenericJson {

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
  @com.google.api.client.util.Key("business_nums")
  private java.util.List<java.lang.String> businessNums;

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
  @com.google.api.client.util.Key
  private java.lang.String name;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("personal_nums")
  private java.util.List<java.lang.String> personalNums;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String place;

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
  public MainRegisterUserMessage setAddress(java.lang.String address) {
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
  public MainRegisterUserMessage setAvailable(java.lang.String available) {
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
  public MainRegisterUserMessage setBio(java.lang.String bio) {
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
  public MainRegisterUserMessage setBirth(java.lang.String birth) {
    this.birth = birth;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getBusinessNums() {
    return businessNums;
  }

  /**
   * @param businessNums businessNums or {@code null} for none
   */
  public MainRegisterUserMessage setBusinessNums(java.util.List<java.lang.String> businessNums) {
    this.businessNums = businessNums;
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
  public MainRegisterUserMessage setCity(java.lang.String city) {
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
  public MainRegisterUserMessage setEmail(java.lang.String email) {
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
  public MainRegisterUserMessage setField(java.lang.String field) {
    this.field = field;
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
  public MainRegisterUserMessage setName(java.lang.String name) {
    this.name = name;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getPersonalNums() {
    return personalNums;
  }

  /**
   * @param personalNums personalNums or {@code null} for none
   */
  public MainRegisterUserMessage setPersonalNums(java.util.List<java.lang.String> personalNums) {
    this.personalNums = personalNums;
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
  public MainRegisterUserMessage setPlace(java.lang.String place) {
    this.place = place;
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
  public MainRegisterUserMessage setSex(java.lang.String sex) {
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
  public MainRegisterUserMessage setSurname(java.lang.String surname) {
    this.surname = surname;
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
  public MainRegisterUserMessage setYearsExp(java.lang.Long yearsExp) {
    this.yearsExp = yearsExp;
    return this;
  }

  @Override
  public MainRegisterUserMessage set(String fieldName, Object value) {
    return (MainRegisterUserMessage) super.set(fieldName, value);
  }

  @Override
  public MainRegisterUserMessage clone() {
    return (MainRegisterUserMessage) super.clone();
  }

}
