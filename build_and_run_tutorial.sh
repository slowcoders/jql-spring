pushd tutorial+sample-app
sh ./db/start_postgres.sh
./gradlew bootRun --console=plain
popd
