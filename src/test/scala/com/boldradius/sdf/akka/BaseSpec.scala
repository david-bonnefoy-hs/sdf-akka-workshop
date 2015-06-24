package com.boldradius.sdf.akka

/**
 * Created by davidb on 15-06-24.
 */

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Inspectors, Matchers, WordSpec}

abstract class BaseSpec extends WordSpec with Matchers with TypeCheckedTripleEquals with Inspectors
