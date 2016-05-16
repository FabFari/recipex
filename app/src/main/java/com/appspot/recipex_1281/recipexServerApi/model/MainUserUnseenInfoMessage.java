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
 * (build: 2016-05-04 15:59:39 UTC)
 * on 2016-05-16 at 13:06:14 UTC 
 * Modify at your own risk.
 */

package com.appspot.recipex_1281.recipexServerApi.model;

/**
 * Model definition for MainUserUnseenInfoMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the recipexServerApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MainUserUnseenInfoMessage extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("num_messages") @com.google.api.client.json.JsonString
  private java.lang.Long numMessages;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("num_prescriptions") @com.google.api.client.json.JsonString
  private java.lang.Long numPrescriptions;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("num_requests") @com.google.api.client.json.JsonString
  private java.lang.Long numRequests;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private MainDefaultResponseMessage response;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getNumMessages() {
    return numMessages;
  }

  /**
   * @param numMessages numMessages or {@code null} for none
   */
  public MainUserUnseenInfoMessage setNumMessages(java.lang.Long numMessages) {
    this.numMessages = numMessages;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getNumPrescriptions() {
    return numPrescriptions;
  }

  /**
   * @param numPrescriptions numPrescriptions or {@code null} for none
   */
  public MainUserUnseenInfoMessage setNumPrescriptions(java.lang.Long numPrescriptions) {
    this.numPrescriptions = numPrescriptions;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getNumRequests() {
    return numRequests;
  }

  /**
   * @param numRequests numRequests or {@code null} for none
   */
  public MainUserUnseenInfoMessage setNumRequests(java.lang.Long numRequests) {
    this.numRequests = numRequests;
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
  public MainUserUnseenInfoMessage setResponse(MainDefaultResponseMessage response) {
    this.response = response;
    return this;
  }

  @Override
  public MainUserUnseenInfoMessage set(String fieldName, Object value) {
    return (MainUserUnseenInfoMessage) super.set(fieldName, value);
  }

  @Override
  public MainUserUnseenInfoMessage clone() {
    return (MainUserUnseenInfoMessage) super.clone();
  }

}
