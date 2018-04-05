Photo Competition - Backend
===========================

## Deploy

If you haven't got one already, create an EC2 Key Pair:  Log into the AWS console, and go to `Services` -> `EC2` ->
`Key Pairs` -> `Create Key Pair`.

Create the Cloud Formation stack:

* Log into AWS console, and go to `services` -> `Cloud Formation` -> `Create Stack`.
* Stack name - `webdev` or any other reasonable name.
* DbPassword - Pick a random password, and make sure it matches the password in `prod-config.yml`.
* VpcId - VPC Id of the default VPC, this is visible from `services` -> `VPC` -> `Your VPCs`, and look for the VPC with
 `Default VPC` set to `Yes`.
* Click `Next`, keep all the defaults, and click `Next` again.
* Acknowledge the checkbox, and click `Create`. 
* Wait for the status of the stack to be `STACK_CREATED`.

Select the stack and look at the `Outputs` tab.  Update `prod-config.yml`:

* `rawImages.s3Bucket` is `S3BucketName`
* `database.url` should contain the `RdsEndpoint`

Record the value of `ElasticIp` for later.

Run `infrastructure/deploy.sh <key location> <ElasticIp>` to build the app and deploy it to the EC2 instance.

The API specification is available via Swagger at `http://<ElasticIP>/api-specification` and there are some
application healthchecks at `http://<ElasticIP>:8081/healthcheck` (this will need the password which is set in
`prod-config.yml`).

### Troubleshooting

Deployment fails with incorrect Java version, or Java not found at all:  AWS' cfn-init seems to just fail sometimes,
ssh to the instance and run `sudo yum install java-1.8.0-openjdk` followed by `sudo update-alternatives --config java`
and choose Java 8.  Run `java -version` and make sure you've got Java 8.  

You may also need to add the iptables rule manually: run `sudo iptables -t nat -L` and if you can't see the 
following line in the `PREROUTING` table,

```REDIRECT  tcp  -- anywhere anywhere tcp dpt:http redir ports 8080``` 

then run

```sudo iptables -t nat -A PREROUTING -i eth0 -p tcp --dport 80 -j REDIRECT --to-port 8080``` 

to add it.  Now redeploy with the shell script.
    
## Development

Run `mvn clean package` to build, test, and package

Update the values of `s3Bucket` and `database.url` in `dev-config.yml` and start with 

```java -jar target/webdev-1.0-SNAPSHOT.jar server dev-config.yml```

You will need a local MySQL server to run in development mode.

## Architecture

A RESTful Java/Dropwizard application hosted on EC2, backed by a MySQL database for image metadata, and an S3 
bucket for the raw image data.

This app supports multiple non-overlapping photo competition websites for multiple students by assigning each student 
a unique token, and all API calls which the students need to make include the token as a query parameter, eg.

```images/random?token=<token> ```

The API calls available to students are things like uploading images, up and down voting images, and getting the 
highest rated image, these API calls are documented with Swagger.  There is also an API protected by a "moderator"
password, which allows the deletion of images.

The API calls which do things like create, delete, and list tokens are password protected by an "admin" password 
as the students don't need this and using it could be disruptive.  There is also a special API call (`bootstrap`)
which creates a token with a bunch of photos already uploaded.

The "admin" and "moderator" passwords are set, along with all other config, in `prod-config.yml`.


                                          EC2
        +-----------------------+       +-------------------+
        |                       |       |                   |
        |                       |       |                   |
        | Client (AJAX website) +------->   Application     +---------------+
        |                       |       |                   |               |
        |                       |       |                   |               |
        +-----------+-----------+       +---------+---------+               |
                    |                             |                         |
                    |                             |                         |
                    |                             |                         |
                    |                             |                         |
                    |                    S3       |                 MySQL on|AWS RDS
                    |                   +---------v---------+     +---------v---------+
                    |                   |                   |     |                   |
                    |                   |                   |     |                   |
                    +------------------->  Raw image data   |     |  Image metadata   |
                                        |                   |     |                   |
                                        |                   |     |                   |
                                        +-------------------+     +-------------------+

