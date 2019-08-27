
Installation Configuration
==========================

Configuration of CoG is handled via the $COG_CONFIG_DIR/cog_settings.cfg
file. There are several environment variables that must be set and many
more that can be set if desired to modify the node configuration.

Values set in this file will not be overridden by future upgrades.

Updating the SITE_NAME
----------------------

If for any reason or of the current node needs to change, the command
below will need to be run (You do not have to do this for any other
variable in the cog_settings.cfg file.)

python manage.py init_site

Configuration Variables
-----------------------

.. csv-table::
   :header:  "Variable", "Meaning", "Example"
   :widths:  20, 20, 50
   :align:   left


   "SITE_NAME", "A short human readable name identifying this specific CoG instance", "SITE_NAME = LOCALHOST"
   "SITE_DOMAIN", "The node domain name and optional port number, without any protocol.", "SITE_DOMAIN = localhost:8000"   
   "TIME_ZONE", "The node specific time zone - see list of Django values for time zones", "TIME_ZONE=America/Denver"
   "SECRET_KEY", "The node specific key: an arbitrary string with lots of strange characters. A random string is 
   generated when CoG is first installed, but you may want to replace it with your own long sequence of random characters
   and keep it secret.","SECRET_KEY=yb@$-bub$i_mr34dda%p=^(f-h&dsdaudssduy040x))19g^iha&#1134df4"

 
