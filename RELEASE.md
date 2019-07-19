# Browser History Histogram for Autopsy - How to launch release

make_release.py is a script to automate new releases. 

First it creates the .nbm and .jar necessary to install install in  [Autopsy](https://www.autopsy.com/) 
or to run as a standalone application, respectively. 

Then it launches a pre-release with two files created previously.


## Getting Started

### Prerequisites
* Go
* Ant, ivy
* Create a [github token](https://help.github.com/articles/creating-an-access-token-for-command-line-use)
* Python3
### Installing

```sh
export GITHUB_TOKEN=...

go get github.com/itchio/gothub
```
    
### Run

```sh
make_release.py <major|minor|patch> 
```

## Authors

* **Kevin Baptista**
* **Tomás Honório**
* Work developed under the guidance and coordination of Professors **Patrício Domingues** and **Miguel Frade**


