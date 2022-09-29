# cucumberjparesttestapi

This package exists to isolate code used to launch a
test SpringBootApplication that uses the jparest package
with a h2 database. The separation of the package was
to provide clean boundaries between test code and the
test web service. This also helps prevent any conflicts
between spring configuration for the test service and
the tests themselves
