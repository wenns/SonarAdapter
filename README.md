
SonarAdapter
============

The SonarAdapter is part of the UQASAR system. It implements the
uQasarAdapter interface and provides an easy access to source code
metrics in Sonar.


Usage
=====
Instantiate the SonarAdapter class and call the method 'query' like follows:

SonarAdapter sonarAdapter = new SonarAdapter(<host>, <port>);
List<Measurement> measurements = sonarAdapter.query(<projectname>, )

Both <host> and <port> are strings; they should specify specify a
working Sonar instance (check it by pointing your browser to
<host>:<port>).

The string <projectname> should be a valid Sonar project resource name.

