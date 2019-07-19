#!/usr/bin/python

import getopt
import os
import sys

import requests

DEFAULT_COMMAND = 'make_release.py <major|minor|patch>'
JAR_LOCATION = './build/dist/Browser-History-Histogram.jar'
NBM_LOCATION = './build/pt-ipleiria-estg-dei-labcif-bhh.nbm'
USER = 'labcif'
REPOSITORY = 'BHH'

# TODO: allow to edit name and description

def main(argv):
    name = ''
    description = ''
    type_release = ''
    try:
        opts, args = getopt.getopt(argv, "hn:d:", ["name=", "description="])
        type_release = argv[0]
    except getopt.GetoptError:
        stop_execution()

    for opt, arg in opts:
        if opt == '-h':
            stop_execution()

    last_version = get_latest()
    new_version = get_new_version(type_release)

    compile_project()
    create_pre_release(name if '' else new_version, description, new_version)
    upload_file_to_release(JAR_LOCATION, 'Browser-History-Histogram.jar', new_version)
    upload_file_to_release(NBM_LOCATION, 'Labcif-bhh-autopsy.nbm', new_version)
    print('Last release was ' + last_version)
    print('New pre-release launched - version: ' + new_version)


def get_latest():
    r = requests.get('https://api.github.com/repos/labcif/bhh/releases/latest')
    return r.json()['tag_name']


def get_new_version(type_release):
    if type_release == 'major':
        version_array = get_latest().split('.')
        major_version = int(version_array[0])
        return str(major_version+1) + '.' + version_array[1] + '.' + version_array[2]
    elif type_release == 'minor':
        version_array = get_latest().split('.')
        minor_version = int(version_array[1])
        return version_array[0] + '.' + str(minor_version+1) + '.' + version_array[2]
    elif type_release == 'patch':
        version_array = get_latest().split('.')
        patch_version = int(version_array[2])
        return version_array[0] + '.' + version_array[1] + '.' + str(patch_version+1)
    else:
        stop_execution()


def stop_execution():
    print(DEFAULT_COMMAND)
    sys.exit()


def compile_project():
    print('Compiling project...')
    print('creating jar...')
    os.system('ant -buildfile standalone.xml main')
    print('jar created')
    print('creating nbm...')
    os.system('ant nbm')
    print('nbm created')


def create_pre_release(name, description, version):
    print('Creating new pre-release version: ' + version + '...')
    os.system('gothub release'
              ' --user ' + USER +
              ' --repo ' + REPOSITORY +
              ' --tag ' + version +
              ' --name "' + name + '"'
              ' --description "' + description + '"'
              ' --pre-release')
    print('done')


def upload_file_to_release(file, filename, version):
    print('Uploading file ' + filename + '...')
    os.system('gothub upload '
              ' --user ' + USER +
              ' --repo ' + REPOSITORY +
              ' --tag ' + version +
              ' --name "' + filename + '"'
              ' --file ' + file)
    print('Upload finished')


if __name__ == "__main__":
    main(sys.argv[1:])
