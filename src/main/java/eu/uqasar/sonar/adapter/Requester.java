package eu.uqasar.sonar.adapter;

import eu.uqasar.adapter.exception.uQasarException;

interface Requester{
  String fetch(String query) throws uQasarException;
}
