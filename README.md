# Python Hello World
Basic Flask API created in Python, intended to learn about some DevOps tools.

## Composition
* **PythonHelloWorld** - Folder with the solution and its unit tests.
* **Dockerfile** - Dockerfile to build the solution.
* **Jenkins files** - Files with Jenkins pipelines, separating branches from master branch.
* **LICENSE** - File with the license, basically it says that you can use the code as you wish.
* **README.md** - This file!
* **sonar-project.properties** - File with configuration to execute Sonarqube analysis during master build.
* **VERSION** - Plain/text file with the version of the solution. It is used to tag the image once it is deployed.

## Usage
The app runs a listener on the port 9999. Using curl, any browser or a rest client, you can get the result.
Being Python 3 previously installed, just execute:
```bash
python hello_world.py
```
And to query the API:
```bash
curl http://127.0.0.1:9999/helloworld
```

## Deployment
The 'Jenkinsfile_master' file has been updated with a new step to deploy the solution in a Docker container.

I created the file 'Dockerfile' to build the image. Basically it copies the Python app and install the required libraries.

When a branch is merged to master, Jenkins builds the image and pushes it onto my own [registry](https://cloud.docker.com/u/davidleonm/repository/docker/davidleonm/pythonhelloworld).

The version is located in the 'VERSION' file. It must be modified manually, I thought about auto-tagging with Jenkins but I think that Jenkins should be readonly. Furthermore, all the Github repositories are configured to allow only signed commits, if Jenkins is able to commit and push, it would imply a security breach.

## Changelog
* **1.2.0** - Update Jenkins files to use shared library.
* **1.1.0** - Update Dockerfile with the lastest version of python and added curl for CI. Updated Jenkins files to add CI
* **First release** - First version of this solution after splitting it.

## License
Use this code as you wish! Totally free to be copied/pasted.