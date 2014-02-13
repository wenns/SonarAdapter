package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;

interface Requester{
  public String fetch(String query) throws uQasarException;
}
