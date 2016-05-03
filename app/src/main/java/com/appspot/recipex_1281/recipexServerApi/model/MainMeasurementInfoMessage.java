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
 * Model definition for MainMeasurementInfoMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the recipexServerApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MainMeasurementInfoMessage extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long bpm;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("chl_level")
  private java.lang.Double chlLevel;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("date_time")
  private java.lang.String dateTime;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double degrees;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long diastolic;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double hgt;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String kind;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String note;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long nrs;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long respirations;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private MainDefaultResponseMessage response;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double spo2;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long systolic;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getBpm() {
    return bpm;
  }

  /**
   * @param bpm bpm or {@code null} for none
   */
  public MainMeasurementInfoMessage setBpm(java.lang.Long bpm) {
    this.bpm = bpm;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getChlLevel() {
    return chlLevel;
  }

  /**
   * @param chlLevel chlLevel or {@code null} for none
   */
  public MainMeasurementInfoMessage setChlLevel(java.lang.Double chlLevel) {
    this.chlLevel = chlLevel;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDateTime() {
    return dateTime;
  }

  /**
   * @param dateTime dateTime or {@code null} for none
   */
  public MainMeasurementInfoMessage setDateTime(java.lang.String dateTime) {
    this.dateTime = dateTime;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getDegrees() {
    return degrees;
  }

  /**
   * @param degrees degrees or {@code null} for none
   */
  public MainMeasurementInfoMessage setDegrees(java.lang.Double degrees) {
    this.degrees = degrees;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getDiastolic() {
    return diastolic;
  }

  /**
   * @param diastolic diastolic or {@code null} for none
   */
  public MainMeasurementInfoMessage setDiastolic(java.lang.Long diastolic) {
    this.diastolic = diastolic;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getHgt() {
    return hgt;
  }

  /**
   * @param hgt hgt or {@code null} for none
   */
  public MainMeasurementInfoMessage setHgt(java.lang.Double hgt) {
    this.hgt = hgt;
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
  public MainMeasurementInfoMessage setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getKind() {
    return kind;
  }

  /**
   * @param kind kind or {@code null} for none
   */
  public MainMeasurementInfoMessage setKind(java.lang.String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getNote() {
    return note;
  }

  /**
   * @param note note or {@code null} for none
   */
  public MainMeasurementInfoMessage setNote(java.lang.String note) {
    this.note = note;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getNrs() {
    return nrs;
  }

  /**
   * @param nrs nrs or {@code null} for none
   */
  public MainMeasurementInfoMessage setNrs(java.lang.Long nrs) {
    this.nrs = nrs;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getRespirations() {
    return respirations;
  }

  /**
   * @param respirations respirations or {@code null} for none
   */
  public MainMeasurementInfoMessage setRespirations(java.lang.Long respirations) {
    this.respirations = respirations;
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
  public MainMeasurementInfoMessage setResponse(MainDefaultResponseMessage response) {
    this.response = response;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getSpo2() {
    return spo2;
  }

  /**
   * @param spo2 spo2 or {@code null} for none
   */
  public MainMeasurementInfoMessage setSpo2(java.lang.Double spo2) {
    this.spo2 = spo2;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getSystolic() {
    return systolic;
  }

  /**
   * @param systolic systolic or {@code null} for none
   */
  public MainMeasurementInfoMessage setSystolic(java.lang.Long systolic) {
    this.systolic = systolic;
    return this;
  }

  @Override
  public MainMeasurementInfoMessage set(String fieldName, Object value) {
    return (MainMeasurementInfoMessage) super.set(fieldName, value);
  }

  @Override
  public MainMeasurementInfoMessage clone() {
    return (MainMeasurementInfoMessage) super.clone();
  }

}
